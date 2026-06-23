/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_notiz_verantwortlicher", columnList = "verantwortlicher_id, id"),
        @Index(name = "ix_notiz_dossier", columnList = "dossier_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Notiz extends AbstractEntity<Notiz> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -1585325151914950816L;

    @NotNull
    @NotEmpty
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    @ToString.Include
    private String titel;


    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_5000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_5000)
    private String bemerkungen;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @ToString.Include
    private NotizStatus status = NotizStatus.ABGESCHLOSSEN;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length =  DbConst.DB_ENUM_LENGTH)
    @ToString.Include
    private NotizTyp typ = NotizTyp.DEFAULT;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_notiz_dossier"), nullable = false)
    private Dossier dossier;

    @NotNull
    @NonNull
    @Valid
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_notiz_benutzer_verantwortlicher"), nullable = false)
    private Benutzer verantwortlicher;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String anlassLink;

    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "Notiz_Label",
        joinColumns = @JoinColumn(
            name = "notiz_id",
            foreignKey = @ForeignKey(name = "fk_notiz_label_notiz")),
        inverseJoinColumns = @JoinColumn(
            name = "label_id",
            foreignKey = @ForeignKey(name = "fk_notiz_label_label")),
        indexes = {
            @Index(name = "ix_notiz_label_notiz",
                columnList = "notiz_id, label_id"),
            @Index(name = "ix_notiz_label_label",
                columnList = "label_id, notiz_id"),
        }
    )
    private Set<Label> labels;

    @NotNull
    @NonNull
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestampErstellt DESC")
    @JoinTable(name = "Notiz_Filedata",
        joinColumns = @JoinColumn(
            name = "notiz_id",
            foreignKey = @ForeignKey(name = "fk_notiz_filedata_notiz")),
        inverseJoinColumns = @JoinColumn(
            name = "filedata_id",
            foreignKey = @ForeignKey(name = "fk_notiz_filedata_filedata")),
        indexes = {
            @Index(name = "ix_notiz_filedata_notiz",
                columnList = "notiz_id"),
            @Index(name = "ix_notiz_filedata_filedata",
                columnList = "filedata_id"),
        }
    )
    private List<FileData> files;

    // copy constructor
    protected Notiz(@NonNull Notiz notiz) {
        setId(notiz.getId());
        setTitel(notiz.getTitel());
        setBemerkungen(notiz.getBemerkungen());
        setStatus(notiz.getStatus());
        setTyp(notiz.getTyp());
        setDossier(notiz.getDossier());
        setVerantwortlicher(notiz.getVerantwortlicher());
        setAnlassLink(notiz.getAnlassLink());
        setLabels(new HashSet<>(notiz.getLabels()));
        // copy file data
        files = new ArrayList<>(notiz.getFiles().size());
        for (FileData fileData : notiz.getFiles()) {
            files.add(new FileData(fileData.getFileTyp(), fileData.getCalculatedFileName(), fileData.getFileInfoBucket().deepCopy()));
        }
    }

    public boolean hasLabel(@NonNull ID<Label> labelId) {
        return getLabels().stream().anyMatch(label -> label.getId().equals(labelId));
    }

    public static Notiz copyFromExisting(
        @NonNull Notiz existing,
        @NonNull String newTitle,
        @NonNull String newBemerkungen
    ) {
        Notiz copy = new Notiz(existing);

        copy.setTitel(newTitle);
        copy.setBemerkungen(newBemerkungen);
        copy.setId(ID.random(Notiz.class)); // set new id
        return copy;
    }


}
