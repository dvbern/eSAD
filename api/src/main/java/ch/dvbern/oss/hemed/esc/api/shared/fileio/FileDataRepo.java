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

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class FileDataRepo extends BaseRepo<FileData> {

    @NonNull
    public FileData getByFileIdInBucket(@NonNull String fileIdInBucket) {
        FileData fileData = db
            .selectFrom(QFileData.fileData)
            .where(QFileData.fileData.fileInfoBucket.fileIdInBucket.eq(fileIdInBucket))
            .fetchFirst();
        if (fileData == null) {
            throw new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(FileData.class, fileIdInBucket));
        }
        return fileData;
    }

    @NonNull
    public List<String> findAllFileIdsInBucket() {
        return db
            .select(QFileData.fileData.fileInfoBucket.fileIdInBucket)
            .from(QFileData.fileData)
            .fetch();
    }

}
