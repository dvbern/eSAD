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

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A wrapper around a {@link File} that implements the {@link FileUpload} interface.
 * This is useful for testing, where we want to create a {@link FileUpload} from a {@link File}.
 */
public class FileUploadWrapper implements FileUpload {

    private final File file;
    private final String fileName;
    private final String contentType;

    public FileUploadWrapper(File file) throws IOException {
        this.file = file;
        this.fileName = file.getName();
        this.contentType = Files.probeContentType(file.toPath());
    }

    @Override
    public String name() {
        return fileName;
    }

    @Override
    public Path filePath() {
        return Path.of(file.getPath());
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public String charSet() {
        return "";
    }

    @Override
    public long size() {
        return file.length();
    }

    @Override
    public Path uploadedFile() {
        return file.toPath();
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return new MultivaluedHashMap<>();
    }

    @NonNull
    public static FileUpload fromFile(@NonNull File file) {
        try {
            return new FileUploadWrapper(file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create FileUpload from file", e);
        }
    }
}
