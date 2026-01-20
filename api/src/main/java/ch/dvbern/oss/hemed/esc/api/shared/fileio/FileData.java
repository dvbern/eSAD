/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileNameConverter;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

import java.io.Serial;

@Entity
@Table
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileData extends AbstractEntity<FileData> {

    @Serial
    private static final long serialVersionUID = 8771476062006091782L;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private FileTyp fileTyp;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Convert(converter = CleanFileNameConverter.class)
    private CleanFileName calculatedFileName;

    @Valid
    @NotNull
    @NonNull
    @Embedded
    private FileInfo fileInfoBucket;

}
