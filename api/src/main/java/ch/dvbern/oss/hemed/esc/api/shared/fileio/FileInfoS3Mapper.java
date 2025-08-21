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

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import lombok.experimental.UtilityClass;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@UtilityClass
public class FileInfoS3Mapper {

    @NonNull
    public static FileInfoS3 mapFileUploadToFileInfoS3(@NonNull FileUpload fileUpload, @NonNull FileTyp fileTyp) {
        CleanFileName cleanFileName = new CleanFileName(fileUpload.fileName());
        return FileInfoS3.of(
            fileTyp.getPrefix(),
            cleanFileName,
            MediaType.parse(fileUpload.contentType()),
            fileUpload.size()
        );
    }
}
