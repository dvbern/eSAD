/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Adds entries to a {@link ZipOutputStream}, fetching their content from S3 and keeping entry names unique.
 * <p>
 * The caller owns the {@link ZipOutputStream} and therefore decides where the archive is written: a
 * {@link java.io.ByteArrayOutputStream} for small archives, a file stream for archives that must not be held on the
 * heap. One instance per archive - it carries the set of entry names used so far.
 */
@RequiredArgsConstructor
@Slf4j
public class S3ZipWriter {

    private final ZipOutputStream zos;
    private final FileStoreService fileStoreService;

    private final Set<String> usedEntryNames = new HashSet<>();

    /**
     * @return the name the entry was actually stored under, which is {@code entryName} unless that collided with an
     * entry already written to this archive.
     */
    @NonNull
    public String addEntry(@NonNull String entryName, byte[] content) throws IOException {
        String uniqueEntryName = FileNameUtil.uniqueEntryName(usedEntryNames, entryName);
        zos.putNextEntry(new ZipEntry(uniqueEntryName));
        zos.write(content);
        zos.closeEntry();
        return uniqueEntryName;
    }

    /**
     * Fetches the file from S3 and adds it as an entry. A file that cannot be read from S3 fails the whole archive.
     *
     * @throws AppFailureException if the file could not be read from S3.
     */
    @NonNull
    public String addFromS3(@NonNull String entryName, @NonNull FileInfo fileInfo) throws IOException {
        return addEntry(entryName, fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket()));
    }

    /**
     * Like {@link #addFromS3}, but tolerates a file that cannot be read from S3 so that one unreadable file does not
     * cost the caller the whole archive.
     *
     * @return {@code false} if the file could not be read; the entry is then skipped and nothing is written. Callers
     * that need to react to the skip (e.g. to leave the corresponding entity untouched) must check the return value.
     */
    public boolean addFromS3IfAvailable(@NonNull String entryName, @NonNull FileInfo fileInfo) throws IOException {
        try {
            addFromS3(entryName, fileInfo);
            return true;
        } catch (AppFailureException e) {
            LOG.error("Could not fetch file from S3 for zip entry [{}]: {}", entryName, e.getMessage(), e);
            return false;
        }
    }
}
