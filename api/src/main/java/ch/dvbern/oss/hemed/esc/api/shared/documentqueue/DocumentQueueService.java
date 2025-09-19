package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.spss.CsvGeneratorService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentQueueService {

    private final DocumentQueueRepo documentQueueRepo;
    private final CurrentUserService currentUserService;
    private final FileDataRepo fileDataRepo;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final AppClock appClock;
    private final CsvGeneratorService csvGeneratorService;
    private final TL tl;

    @NonNull
    @Transactional(TxType.REQUIRED)
    public List<DocumentQueue> findAllUnfinished(long limit) {
        return documentQueueRepo.findAllUnfinished(limit);
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public List<DocumentQueue> findAll() {
        return documentQueueRepo.findAll();
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public List<DocumentQueue> findAllByCurrentBenutzer() {
        return documentQueueRepo.findAllByBenutzer(currentUserService.currentUserID());
    }

    @NonNull
    @Transactional(TxType.REQUIRED)
    public List<DocumentQueue> findAllUnfinishedByCurrentBenutzer() {
        return documentQueueRepo.findAllUnfinishedByBenutzer(currentUserService.currentUserID());
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void update(@NonNull DocumentQueue documentQueue) {
        DocumentQueue managedEntity = getById(documentQueue.getId());
        managedEntity.setStatus(documentQueue.getStatus());
        managedEntity.setLastError(documentQueue.getLastError());
        managedEntity.setTimestampResult(documentQueue.getTimestampResult());
        managedEntity.setErrorCount(documentQueue.getErrorCount());

        documentQueueRepo.persist(managedEntity);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void markAsInProgress(@NonNull ID<DocumentQueue> id) {
        DocumentQueue managedEntity = getById(id);
        managedEntity.setStatus(DocumentQueueStatus.IN_PROGRESS);
        documentQueueRepo.persist(managedEntity);
    }

    @Transactional(TxType.REQUIRED)
    public void addJob(@NonNull DocumentQueue documentQueue) {
        documentQueueRepo.persist(documentQueue);
    }

    @NonNull
    public DocumentQueue getById(@NonNull ID<DocumentQueue> id) {
        return documentQueueRepo.getById(id);
    }

    @NonNull
    public FileInfoS3 findUploadedFileInfoS3(@NonNull ID<DocumentQueue> id) {
        FileData fileData = getById(id).getResult();
        Objects.requireNonNull(fileData);

        return fileData.getFileInfoBucket();
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public byte[] triggerGenerationFunction(@NonNull DocumentQueue documentQueue) {
        switch (documentQueue.getType()) {
            case PLAYGROUND -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AppFailureException(AppFailureMessage.internalError("Error while sleeping"), e);
                }
                try (InputStream resourceAsStream = DocumentQueueService.class.getResourceAsStream(
                    "/playground-document/playground.pdf")) {
                    Objects.requireNonNull(resourceAsStream);
                    return resourceAsStream.readAllBytes();
                } catch (IOException e) {
                    throw new AppFailureException(AppFailureMessage.internalError("Error reading playground PDF"), e);
                }
            }
            case PLAYGROUND_ERROR -> {
                // This will always error out.
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AppFailureException(AppFailureMessage.internalError("Error while sleeping"), e);
                }

                throw new AppFailureException(AppFailureMessage.internalError("Expected playground document queue error"));
            }
            case SPSS_REPORT -> {
                // Extract payload
                DocumentQueueParams parameters = unmarshalDocumentQueueParameter(documentQueue);
                Objects.requireNonNull(parameters.getSpssReportType());
                return csvGeneratorService.generateSpssReport(parameters.getSpssReportType(), documentQueue.getBenutzer().getEmail());
            }
            default -> throw new AppFailureException(AppFailureMessage.internalError(
                "Unhandled FileType in DocumentQueue generator: " + documentQueue.getType()));
        }
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void storeResult(@NonNull ID<DocumentQueue> id, byte[] content) {
        DocumentQueue documentQueue = getById(id);
        LocalDate today = appClock.localDateTime().now().toLocalDate();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            List.of(),
            today,
            getBaseFilename(documentQueue),
            documentQueue.getType(),
            getFileExtension(documentQueue));

        Path tempFilePath = createTmpFile(calculatedFileName.toString(), content);
        FileUpload fileUpload = FileUtil.fileToFileUpload(tempFilePath, calculatedFileName.toString());
        FileInfoS3 fileInfoS3 = FileInfoS3Mapper.mapFileUploadToFileInfoS3(fileUpload, documentQueue.getType());

        fileStoreService.uploadToS3Async(fileUpload, fileInfoS3)
            .await()
            .atMost(configService.getS3UploadTimeout());

        FileData filedata = new FileData(documentQueue.getType(), calculatedFileName, fileInfoS3);
        fileDataRepo.persist(filedata);

        documentQueue.setResult(filedata);
        documentQueueRepo.persist(documentQueue);
    }

    @NonNull
    private String getBaseFilename(@NonNull DocumentQueue documentQueue) {
        return switch (documentQueue.getType()) {
            case PLAYGROUND -> "Playground-File";
            case PLAYGROUND_ERROR -> "Playground-Error-File";
            case SPSS_REPORT -> {
                DocumentQueueParams params = unmarshalDocumentQueueParameter(documentQueue);
                Objects.requireNonNull(params.getSpssReportType());
                String suffix = tl.translateValue(params.getSpssReportType());

                yield "Spss-Report-" + suffix;
            }
            default -> throw new AppFailureException(AppFailureMessage.internalError(
                "Unhandled FileType in DocumentQueue getNameVorname switch: " + documentQueue.getType()));
        };
    }

    @NonNull
    private String getFileExtension(@NonNull DocumentQueue documentQueue) {
        return switch (documentQueue.getType()) {
            case PLAYGROUND, PLAYGROUND_ERROR -> ".pdf";
            case SPSS_REPORT -> ".csv";
            default -> throw new AppFailureException(AppFailureMessage.internalError(
                "Unhandled FileType in DocumentQueue getFileExtension switch: " + documentQueue.getType()));
        };
    }

    @NonNull
    private Path createTmpFile(@NonNull String fileName, byte[] content) {
        try {
            Path tempFileLoc =
                Files.createTempFile(FilenameUtils.getBaseName(fileName), '.' + FilenameUtils.getExtension(fileName));
            tempFileLoc.toFile().deleteOnExit();

            FileUtils.writeByteArrayToFile(tempFileLoc.toFile(), content);
            return tempFileLoc;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while creating temp file"), e);
        }
    }

    @NonNull
    public static DocumentQueueParams unmarshalDocumentQueueParameter(@NonNull DocumentQueue documentQueue) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (documentQueue.getParameters() != null) {
                return objectMapper.readValue(documentQueue.getParameters(), DocumentQueueParams.class);
            }
            return new DocumentQueueParams();
        } catch (JsonProcessingException e) {
            throw new AppFailureException(AppFailureMessage.internalError(
                "Could not convert DocumentQueue parameter string to object" + e.getMessage()));
        }
    }

    @Transactional(TxType.REQUIRED)
    public long cleanUpOldDocumentQueue() {
        OffsetDateTime cutoffDateTime =
            appClock.offsetDateTime().now().minus(configService.getDocumentqueueMaxLifeTime());

        List<DocumentQueue> documentQueuesToDelete = documentQueueRepo.findAllFinishedAndOlderThan(cutoffDateTime);

        removeDocumentQueueItems(documentQueuesToDelete);
        return documentQueuesToDelete.size();
    }

    public void removeDocumentQueueItems(@Nonnull List<DocumentQueue> documentQueuesToDelete) {
        for (DocumentQueue documentQueue : documentQueuesToDelete) {
            if (documentQueue.getResult() != null) {
                Uni<DeleteObjectResponse> deleteObjectResponseUni =
                    fileStoreService.deleteInS3Async(documentQueue.getResult().getFileInfoBucket().getFileIdInBucket());

                DeleteObjectResponse deleteResponse =
                    deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
                LOG.info("Deleted file from S3: {}", deleteResponse);
            }
            documentQueueRepo.remove(documentQueue.getId());
        }
    }
}
