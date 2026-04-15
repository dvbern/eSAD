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

package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.arzt.ArztService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueFileService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class MassenbriefGeneratorService {

    private final KindService kindService;
    private final ArztService arztService;
    private final BriefvorlageService briefvorlageService;
    private final BriefgenerierungService briefgenerierungService;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final DocumentQueueFileService documentQueueFileService;

    @Transactional(TxType.NOT_SUPPORTED)
    public Path generateMassenbriefZip(
        @NonNull DocumentQueueMassenbriefSaveJson params,
        @NonNull Benutzer benutzer,
        @NonNull CleanFileName fileName
    ) {
        List<FileInfo> fileInfosInBucket = new ArrayList<>();
        List<ID<Kind>> kinderIds = params.getKinderIds();
        Briefvorlage briefvorlage = briefvorlageService.getById(params.getBriefvorlageID());
        Arzt arzt = params.getArztID() == null ? null : arztService.getById(params.getArztID());

        if (briefvorlage.getType() == BriefvorlageType.ANLASS) {
            throw new AppValidationException(AppValidationMessage.errorMassenbriefgenerierung(
                "Massenbriefgenerierung for BriefvorlageTyp.ANLASS is not supported."
            ));
        }
        try {
            StringBuilder kinderIdsSB = new StringBuilder();
            kinderIds.forEach(kindID -> {
                Kind kind = kindService.getById(kindID);
                PrintResultDTO result =
                    briefgenerierungService.generateBriefForKind(briefvorlage, kind, arzt, benutzer);
                kinderIdsSB.append(kindID.getUUID().toString()).append(',');

                FileInfo fileInfo = result.getFileInfo();
                Uni<PutObjectResponse> putObjectResponseUni =
                    fileStoreService.uploadToS3Async(
                        fileInfo.getMediaType().toString(),
                        result.getContent(),
                        fileInfo);
                fileInfosInBucket.add(fileInfo);
                PutObjectResponse putObjectResponse =
                    putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
                LOG.info(
                    "Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}",
                    fileInfo.getFileIdInBucket(),
                    putObjectResponse);
            });

            handleDuplicateFileNames(fileInfosInBucket);
            Path zipFile = createZipFromFileIdsInBucket(fileInfosInBucket, fileName);

            String printedIds = kinderIdsSB.isEmpty() ?
                kinderIdsSB.toString() :
                kinderIdsSB.substring(0, kinderIdsSB.length() - 1);
            logMassenbriefZIPGenerated(printedIds, briefvorlage.getName(), benutzer.getEmail());

            return zipFile;

        } catch (Exception e) {
            LOG.error("ESC-DOCUMENT-QUEUE: Stopped Massenbrief generation due to an unexpected error: ", e);
            throw new AppFailureException(
                AppFailureMessage.internalError("Error while generating Massenbrief files: "), e);

        } finally {
            if (!fileInfosInBucket.isEmpty()) {
                fileInfosInBucket.forEach(fileIdInBucket -> {
                    Uni<DeleteObjectResponse> deleteObjectResponseUni =
                        fileStoreService.deleteInS3Async(fileIdInBucket.getFileIdInBucket());

                    DeleteObjectResponse deleteResponse =
                        deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
                    LOG.info("Deleted single file for Massenbrief from S3: {}", deleteResponse);
                });
            }
        }
    }

    protected Path createZipFromFileIdsInBucket(
        @NonNull @NotEmpty List<FileInfo> fileInfosInBucket,
        @NonNull CleanFileName fileName
    ) {
        Path tempFile = documentQueueFileService.createTempFile(fileName);
        try {

            try (OutputStream os = Files.newOutputStream(tempFile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                ZipOutputStream zos = new ZipOutputStream(bos)) {

                for (FileInfo fileInfo : fileInfosInBucket) {
                    byte[] bytes = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
                    ZipEntry zipEntry = new ZipEntry(fileInfo.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }

            return tempFile;
        } catch (Exception e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Error while creating ZIP from Massenbrief files."), e);
        }
    }

    protected void handleDuplicateFileNames(@NonNull @NotEmpty List<FileInfo> fileInfos) {
        Map<String, Long> nameCount = fileInfos.stream()
            .collect(Collectors.groupingBy(
                fi -> fi.getFileName().toString(),
                Collectors.counting()
            ));

        Map<String, Integer> nameCurrentIndex = new HashMap<>();
        for (FileInfo fileInfo : fileInfos) {
            String name = fileInfo.getFileName().toString();
            if (nameCount.get(name) > 1) {
                int index = nameCurrentIndex.merge(name, 1, Integer::sum);
                if (index > 1) {
                    String baseName = name.contains(".")
                        ? name.substring(0, name.lastIndexOf('.'))
                        : name;
                    String extension = name.contains(".")
                        ? name.substring(name.lastIndexOf('.'))
                        : "";
                    fileInfo.setFileName(CleanFileName.parse(baseName + "_(" + index + ')' + extension));
                }
            }
        }
    }

    private void logMassenbriefZIPGenerated(
        @NonNull String kinderIds,
        @NonNull String briefvorlage,
        @NonNull EmailAddress emailAddress
    ) {
        String template = "ESC-ZIP: Generation of Massenbrief-ZIP for Briefvorlage: typ=${briefvorlage} "
            + "for kinderIds=${kinderIds}; triggering user was "
            + "username=${emailAddress}";
        Map<String, String> params = new HashMap<>();
        params.put("briefvorlage", briefvorlage);
        params.put("kinderIds", kinderIds);
        params.put("emailAddress", emailAddress.toString());

        String result = StringSubstitutor.replace(template, params, "${", "}");
        LOG.info(result);
    }
}
