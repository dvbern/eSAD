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

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileDataMapperTest {

    @Test
    void toJson_shouldMapAllFields() {
        // ARRANGE
        FileInfoS3 fileInfoS3 = FileInfoS3.of(
            "prefix",
            new CleanFileName("file-name.pdf"),
            new MediaType("application", "pdf"),
            2900
        );

        FileData fileData = new FileData(FileTyp.NOTIZUPLOAD, "11-3-2025_Max-Musterman.pdf", fileInfoS3);
        ID<FileData> id = ID.random(FileData.class);
        fileData.setId(id);
        TestUtil.setEntityInfo(fileData);

        // ACT
        FileDataJson fileDataJson = FileDataMapper.toJson(fileData);

        // ASSERT
        assertNotNull(fileDataJson);
        assertEquals(id, fileDataJson.getId());
        assertEquals(fileData.getFileTyp(), fileDataJson.getFileTyp());
        assertEquals(fileData.getCalculatedFileName(), fileDataJson.getCalculatedFileName());
        assertEquals(fileInfoS3.getFileName(), fileDataJson.getFileName());
        assertEquals(fileInfoS3.getFileSize(), fileDataJson.getFileSize());
        assertEquals(fileInfoS3.getMediaType().toString(), fileDataJson.getMimeType());
        assertEquals(fileInfoS3.getFileIdInBucket(), fileDataJson.getFileIdInBucket());
    }
}
