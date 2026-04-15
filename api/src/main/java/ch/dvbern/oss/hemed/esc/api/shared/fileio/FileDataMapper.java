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

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
public final class FileDataMapper {

    public static final String PREFIX_DATENVERLUST = "Datenverlust_";

    private FileDataMapper() {
    }

    @Nullable
    public static FileDataJson toJson(@Nullable FileData data) {
        if (data == null) {
            return null;
        }
        return new FileDataJson(
            data.getId(),
            EntityInfoJson.fromEntity(data),
            data.getFileTyp(),
            data.getFileInfoBucket().getFileName(),
            data.getCalculatedFileName().toString(),
            data.getFileInfoBucket().getFileSize(),
            data.getFileInfoBucket().getMediaType().toString(),
            data.getFileInfoBucket().getFileIdInBucket(),
            data.getCalculatedFileName().toString().startsWith(PREFIX_DATENVERLUST)
        );
    }
}
