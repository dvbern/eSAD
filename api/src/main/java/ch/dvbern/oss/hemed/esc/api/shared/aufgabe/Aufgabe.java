package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.io.Serial;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckAllowedLink;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_aufgabe_benutzer_usererfasst", columnList = "userErfasst_id, id"),
        @Index(name = "ix_aufgabe_benutzer_empfaenger", columnList = "empfaenger_id, id"),
        @Index(name = "ix_aufgabe_status_benutzer_empfaenger", columnList = "aufgabeStatus, empfaenger_id, id"),
        @Index(name = "ix_aufgabe_kind_id", columnList = "kind_id, id"),
        @Index(name = "ix_aufgabe_anlass_id", columnList = "anlass_id, id")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Aufgabe extends AbstractTenantEntity<Aufgabe> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 360072862301835622L;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String titel;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_5000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_5000)
    private String bemerkung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AufgabeStatus aufgabeStatus = AufgabeStatus.OFFEN;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate faelligBis;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_aufgabe_benutzer_erfasst"), nullable = false, updatable = false)
    private Benutzer userErfasst;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_aufgabe_benutzer_empfaenger"), nullable = false)
    private Benutzer empfaenger;

    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "Aufgabe_Label",
        joinColumns = @JoinColumn(
            name = "aufgabe_id",
            foreignKey = @ForeignKey(name = "fk_aufgabe_label_aufgabe")),
        inverseJoinColumns = @JoinColumn(
            name = "label_id",
            foreignKey = @ForeignKey(name = "fk_aufgabe_label_label")),
        indexes = {
            @Index(name = "ix_aufgabe_label_aufgabe",
                columnList = "aufgabe_id, label_id"),
            @Index(name = "ix_aufgabe_label_label",
                columnList = "label_id, aufgabe_id"),
        }
    )
    private Set<Label> labels;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_aufgabe_kind"), nullable = true)
    private Kind kind;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_aufgabe_anlass"), nullable = true)
    private Anlass anlass;

    @NotNull
    @NonNull
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "Aufgabe_Filedata",
        joinColumns = @JoinColumn(
            name = "aufgabe_id",
            foreignKey = @ForeignKey(name = "fk_aufgabe_filedata_aufgabe")),
        inverseJoinColumns = @JoinColumn(
            name = "filedata_id",
            foreignKey = @ForeignKey(name = "fk_aufgabe_filedata_filedata")),
        indexes = {
            @Index(name = "ix_aufgabe_filedata_aufgabe",
                columnList = "aufgabe_id"),
            @Index(name = "ix_aufgabe_filedata_filedata",
                columnList = "filedata_id"),
        }
    )
    private List<FileData> files;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @CheckAllowedLink
    private URL link;

}
