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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.skripthelper;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassConfigSkriptJson {

    @NonNull
    @Schema(required = false)
    private final ID<AnlassConfig> id;

    @Nullable
    @Schema(required = false)
    private final String sqlInsertQuery;

    @Nullable
    @Schema(required = false)
    private final String sqlUpdateQuery;


    @NonNull
    public String getFileContent() {
        StringBuilder sb = new StringBuilder();
        if (sqlInsertQuery != null) {
            sb.append("-- INSERT").append('\n');
            sb.append(sqlInsertQuery).append('\n');
        }
        if (sqlUpdateQuery != null) {
            sb.append("-- UPDATE").append('\n');
            sb.append(sqlUpdateQuery).append('\n');
        }
        return sb.toString();
    }
}
