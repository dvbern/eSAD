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

package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This class represents an archived document, it stores a relation to the archivdocument and
 * 'weak' link to the betroffenesKind and the Dossier
 */
@Entity
@Table(
    indexes = {
        @Index(name = "ix_archivdokument_filedata", columnList = "filedata_id, id"),
        @Index(name = "ix_archivdokument_benutzer", columnList = "owner_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Archivdokument extends AbstractEntity<Archivdokument> {

    @Serial
    private static final long serialVersionUID = -1585325151914950816L;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampArchiviert;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private ArchivierungTyp archivierungTyp;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private ArchivierungStatus archivierungStatus;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private UUID dossierID;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String betroffenesKind; // this is to store a 'readable' representation of the Kind (used for display in table after kind deletion)

    @Nullable // Filedata wird mit dem Batchjob (bzw. Löschung des Kindes) gelöscht
    @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_archivdokument_filedata"), nullable = true, updatable = true)
    private FileData filedata;

    @NotNull
    @NonNull
    @Valid
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_archivdokument_benutzer"), nullable = false)
    private Benutzer owner;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampDownloaded;
}
