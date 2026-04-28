package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.MassenbriefGeneratorService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.spss.CsvGeneratorService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentQueueService {

    private final DocumentQueueFileService documentQueueFileService;
    private final DocumentQueueRepo documentQueueRepo;
    private final CurrentUserService currentUserService;
    private final FileDataRepo fileDataRepo;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final MassenbriefGeneratorService massenbriefGeneratorService;
    private final AppClock appClock;
    private final CsvGeneratorService csvGeneratorService;
    private final FileDataService fileDataService;
    private final BenutzerService benutzerService;

    private static final String ERROR_BASE_MESSAGE = "Could not convert DocumentQueue parameter string to object: ";


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
    public FileInfo findUploadedFileInfo(@NonNull ID<DocumentQueue> id) {
        FileData fileData = getById(id).getResult();
        Objects.requireNonNull(fileData);

        return fileData.getFileInfoBucket();
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public Path triggerGenerationFunction(@NonNull DocumentQueue documentQueue) {
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
                CleanFileName fileName = documentQueueFileService.getCleanFileNameFromDocumentQueue(documentQueue);
                Objects.requireNonNull(resourceAsStream);
                return documentQueueFileService.createTempFile(fileName, resourceAsStream.readAllBytes());
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
            CleanFileName fileName = documentQueueFileService.getCleanFileNameFromDocumentQueue(documentQueue);
            DocumentQueueParams parameters = unmarshalDocumentQueueParameter(documentQueue);
            Objects.requireNonNull(parameters.getSpssReportType());
            return csvGeneratorService.generateSpssReport(
                parameters.getSpssReportType(),
                documentQueue.getBenutzer().getEmail(),
                fileName);
        }
        case CSV_REPORT -> {
            // Extract payload
            CleanFileName fileName = documentQueueFileService.getCleanFileNameFromDocumentQueue(documentQueue);
            DocumentQueueCsvParams parameters = unmarshalDocumentQueueCsvParameter(documentQueue);

            Objects.requireNonNull(parameters.getCsvReportType());
            Objects.requireNonNull(documentQueue.getDatumBis());
            Objects.requireNonNull(documentQueue.getDatumVon());

            DateRange zeitraum = new DateRange();
            zeitraum.setDatumBis(documentQueue.getDatumBis());
            zeitraum.setDatumVon(documentQueue.getDatumVon());

            return csvGeneratorService.generateCsvReport(
                parameters.getCsvReportType(),
                zeitraum,
                documentQueue.getBenutzer().getEmail(),
                fileName);
        }
        case MASSENBRIEF_ZIP -> {
            CleanFileName fileName = documentQueueFileService.getCleanFileNameFromDocumentQueue(documentQueue);
            DocumentQueueMassenbriefSaveJson params = unmarshallDocumentQueueMassenbriefParameter(documentQueue);

            // need to load berechtigungen for user
            Benutzer benutzer = benutzerService.getByIdWithBerechtigungen(documentQueue.getBenutzer().getId());
            return massenbriefGeneratorService.generateMassenbriefZip(params, benutzer, fileName);
        }
        default -> throw new AppFailureException(AppFailureMessage.internalError(
            "Unhandled FileType in DocumentQueue generator: " + documentQueue.getType()));
        }
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void storeResult(@NonNull ID<DocumentQueue> id, Path filePath) {
        DocumentQueue documentQueue = getById(id);
        CleanFileName cleanFileName = CleanFileName.parse(filePath);

        FileUpload fileUpload = FileUtil.fileToFileUpload(filePath, cleanFileName.toString());
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfo(fileUpload, documentQueue.getType());

        fileStoreService.uploadToS3Async(fileUpload, fileInfo)
            .await()
            .atMost(configService.getS3UploadTimeout());

        FileData filedata = new FileData(documentQueue.getType(), cleanFileName, fileInfo);
        fileDataRepo.persist(filedata);

        documentQueue.setResult(filedata);
        documentQueueRepo.persist(documentQueue);
    }

    @NonNull
    public static DocumentQueueParams unmarshalDocumentQueueParameter(@NonNull DocumentQueue documentQueue) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (documentQueue.getParameters() != null) {
                return objectMapper.readValue(documentQueue.getParameters(), DocumentQueueParams.class);
            }
            return new DocumentQueueParams(null);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(AppFailureMessage.internalError(ERROR_BASE_MESSAGE + e.getMessage()));
        }
    }

    @NonNull
    public static DocumentQueueCsvParams unmarshalDocumentQueueCsvParameter(@NonNull DocumentQueue documentQueue) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (documentQueue.getParameters() != null) {
                return objectMapper.readValue(documentQueue.getParameters(), DocumentQueueCsvParams.class);
            }
            return new DocumentQueueCsvParams(null);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(AppFailureMessage.internalError(ERROR_BASE_MESSAGE + e.getMessage()));
        }
    }

    @NonNull
    public static DocumentQueueMassenbriefSaveJson unmarshallDocumentQueueMassenbriefParameter(
        @NonNull DocumentQueue documentQueue
    ) {
        try {
            if (isNotNullOrEmpty(documentQueue.getParameters())) {
                ID<Briefvorlage> briefvorlageID = null;
                ID<Arzt> arztID = null;
                List<ID<Kind>> kinderIds = new ArrayList<>();
                ObjectMapper objectMapper = new ObjectMapper();
                DocumentQueueMassenbriefParams params = objectMapper
                    .readValue(documentQueue.getParameters(), DocumentQueueMassenbriefParams.class);

                if (isNotNullOrEmpty(params.getBriefvorlageID()) &&
                    params.getKinderIds() != null &&
                    !params.getKinderIds().isEmpty()
                ) {
                    briefvorlageID = ID.of(UUID.fromString(params.getBriefvorlageID()), Briefvorlage.class);
                    for (String kindId : params.getKinderIds()) {
                        kinderIds.add(ID.of(UUID.fromString(kindId), Kind.class));
                    }
                    if (isNotNullOrEmpty(params.getArztID())) {
                        arztID = ID.of(UUID.fromString(params.getArztID()), Arzt.class);
                    }
                }
                if (briefvorlageID != null && !kinderIds.isEmpty()) {
                    return new DocumentQueueMassenbriefSaveJson(
                        briefvorlageID,
                        arztID,
                        kinderIds
                    );
                }
            }
            throw new AppValidationException(AppValidationMessage.errorMassenbriefgenerierung(
                "Cannot generate Massenbrief if required parameters are null. DocumentQueueParameters: "
                    + documentQueue.getParameters()
            ));
        } catch (Exception e) {
            throw new AppFailureException(AppFailureMessage.internalError(ERROR_BASE_MESSAGE + e.getMessage()
            ));
        }
    }

    private static boolean isNotNullOrEmpty(@Nullable String stringToCheck) {
        return stringToCheck != null && !stringToCheck.isEmpty();
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
            documentQueueRepo.remove(documentQueue.getId());
            if (documentQueue.getResult() != null) {
                fileDataService.deleteFileData(List.of(documentQueue.getResult()), true);
            }
        }
    }
}
