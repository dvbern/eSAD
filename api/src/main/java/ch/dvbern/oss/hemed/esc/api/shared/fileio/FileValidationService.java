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

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUploadValidationUtil;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This Service should be used to check if we can accept a fileupload
 */
@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final ConfigService configService;

    /**
     * Checks if the file is allowed to be uploaded
     *
     * @param fileUpload the file to be uploaded
     * @throws AppValidationException if the file validation fails due to missing name or type or disallowed type
     */
    public void requireFiletypeIsAllowed(@NonNull FileUpload fileUpload, @NonNull FileTyp fileuploadType) {
        Set<String> whitelist = configService.getFileTypeWhitelistedFileExtensionsForUploadType(fileuploadType);
        Set<MediaType> whitelistedTypes = whitelist.stream()
            .map(FileUtil::parseMediaTypeOrFileExtensionToMediaType)
            .collect(Collectors.toSet());
        FileUploadValidationUtil.validateNameAndType(fileUpload, whitelistedTypes);
    }
}
