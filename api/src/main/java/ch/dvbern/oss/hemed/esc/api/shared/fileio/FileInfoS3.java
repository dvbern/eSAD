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
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;

@Setter
@Getter
@Embeddable
@MappedSuperclass
public class FileInfoS3 extends FileInfo {

    @Serial
    private static final long serialVersionUID = -661141665899629276L;

    /**
     * This is the id of the file in the s3 bucket
     */
    @NotNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @JsonIgnore
    private String fileIdInBucket;

    public FileInfoS3() {
        super();
    }

    private FileInfoS3(
        @NonNull CleanFileName fileName,
        long fileSize,
        @NonNull MediaType mediaType,
        @NonNull String fileIdInBucket
    ) {
        super(fileName, fileSize, mediaType);
        this.fileIdInBucket = fileIdInBucket;
    }

    @NonNull
    public static FileInfoS3 of(
        @NonNull String prefix,
        @NonNull CleanFileName fileName,
        @NonNull MediaType mediaType,
        long fileSize
    ) {
        return new FileInfoS3(
            fileName,
            fileSize,
            mediaType,
            FileUtil.generateUUIDWithOriginalFilename(prefix, fileName)
        );
    }

    @NonNull
    public static FileInfoS3 of(
        @NonNull CleanFileName fileName,
        @NonNull MediaType mediaType,
        long fileSize
    ) {
        return new FileInfoS3(
            fileName,
            fileSize,
            mediaType,
            FileUtil.generateUUIDWithFileExtension(fileName)
        );
    }

    @NonNull
    public static FileInfoS3 of(@NonNull FileInfo fileInfo) {
        return FileInfoS3.of(
            fileInfo.getFileName(),
            fileInfo.getMediaType(),
            fileInfo.getFileSize()
        );
    }

    public FileInfoS3 deepCopy() {
        return new FileInfoS3(this.getFileName(), this.getFileSize(), this.getMediaType(), this.getFileIdInBucket());
    }
}
