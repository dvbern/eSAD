package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageService;
import ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportType;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueCsvSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueDisplayJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueSpssSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class DocumentQueueMapper {

    private final CurrentUserService currentUserService;
    private final DocumentQueueService documentQueueService;
    private final AppLanguageProvider appLanguageProvider;

    private final ObjectMapper objectMapper;
    private final ConfigService configService;
    private final TL tl;
    private final BriefvorlageService briefvorlageService;

    @NonNull
    public static DocumentQueueDisplayJson toJson(@NonNull DocumentQueue documentQueue) {

        FileData fileData = documentQueue.getResult();

        return new DocumentQueueDisplayJson(
            documentQueue.getId(),
            EntityInfoJson.fromEntity(documentQueue),
            documentQueue.getType(),
            documentQueue.getDisplayName(),
            documentQueue.getStatus(),
            documentQueue.getTimestampResult(),
            fileData != null ? fileData.getCalculatedFileName().toString() : null,
            documentQueue.getLastError()
        );
    }

    @NonNull
    public DocumentQueue getById(@NonNull ID<DocumentQueue> id) {
        return documentQueueService.getById(id);
    }

    @NonNull
    public DocumentQueueListJson findAllByCurrentBenutzer() {
        return new DocumentQueueListJson(documentQueueService.findAllByCurrentBenutzer()
            .stream()
            .map(DocumentQueueMapper::toJson)
            .toList());
    }

    @NonNull
    public DocumentQueueListJson findAll() {
        return new DocumentQueueListJson(documentQueueService.findAll()
            .stream()
            .map(DocumentQueueMapper::toJson)
            .toList());
    }

    public void create(@NonNull DocumentQueueSpssSaveJson saveJson) {
        throwExceptionIfUserHasToManyQueuedItems();

        DocumentQueue documentQueue = new DocumentQueue();
        documentQueue.setType(saveJson.getType());
        if (saveJson.getDocumentQueueParams() != null) {
            documentQueue.setDisplayName(getDisplayName(
                saveJson.getType(),
                saveJson.getDocumentQueueParams().getSpssReportType(),
                null,
                null));
        } else {
            documentQueue.setDisplayName("-");
        }
        documentQueue.setBenutzer(currentUserService.currentUser());
        documentQueue.setLanguage((AppLanguages) appLanguageProvider.current());
        documentQueue.setDatumBis(null);
        documentQueue.setDatumVon(null);

        try {
            String jsonString = objectMapper.writeValueAsString(saveJson.getDocumentQueueParams());
            documentQueue.setParameters(jsonString);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError(
                    "Could not store json parameter for doc creation"), e);
        }

        documentQueueService.addJob(documentQueue);
    }

    public void createCsv(@NonNull DocumentQueueCsvSaveJson saveJson) {
        throwExceptionIfUserHasToManyQueuedItems();

        DocumentQueue documentQueue = new DocumentQueue();
        documentQueue.setType(saveJson.getType());
        documentQueue.setDisplayName(getDisplayName(
            documentQueue.getType(),
            null,
            saveJson.getDocumentQueueCsvParams().getCsvReportType(),
            null));
        documentQueue.setBenutzer(currentUserService.currentUser());
        documentQueue.setLanguage((AppLanguages) appLanguageProvider.current());
        documentQueue.setDatumVon(saveJson.getDatumVon());
        documentQueue.setDatumBis(saveJson.getDatumBis());

        try {
            String jsonString =
                objectMapper.writeValueAsString(saveJson.getDocumentQueueCsvParams());
            documentQueue.setParameters(jsonString);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError(
                    "Could not store json parameter for doc creation"), e);
        }

        documentQueueService.addJob(documentQueue);
    }

    public void createMassenbrief(@NonNull DocumentQueueMassenbriefSaveJson saveJson) {
        throwExceptionIfUserHasToManyQueuedItems();

        DocumentQueue documentQueue = new DocumentQueue();
        documentQueue.setType(FileTyp.MASSENBRIEF_ZIP);
        Briefvorlage briefvorlage = briefvorlageService.getById(saveJson.getBriefvorlageID());
        documentQueue.setDisplayName(getDisplayName(documentQueue.getType(), null, null, briefvorlage.getName()));
        documentQueue.setBenutzer(currentUserService.currentUser());
        documentQueue.setLanguage((AppLanguages) appLanguageProvider.current());

        try {
            String jsonString = objectMapper.writeValueAsString(saveJson);
            documentQueue.setParameters(jsonString);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError(
                    "Could not store json parameter for Massenbrief creation"), e);
        }

        documentQueueService.addJob(documentQueue);
    }

    public void removeDocumentQueueItems(@NonNull List<ID<DocumentQueue>> list) {
        List<DocumentQueue> documentsToRemove = list.stream()
            .map(documentQueueService::getById)
            .toList();
        documentQueueService.removeDocumentQueueItems(documentsToRemove);
    }

    @NonNull
    public String getDisplayName(
        @NonNull FileTyp fileTyp,
        @Nullable SpssReportType spssReportType,
        @Nullable CsvReportType csvReportType,
        @Nullable String briefvorlageName
    ) {
        return switch (fileTyp) {
            case FileTyp.SPSS_REPORT -> {
                if (spssReportType != null) {
                    yield tl.translateValue(spssReportType);
                }
                yield "-";
            }
            case FileTyp.CSV_REPORT -> {
                if (csvReportType != null) {
                    yield tl.translateValue(csvReportType);
                }
                yield "-";
            }
            case FileTyp.MASSENBRIEF_ZIP -> {
                if (briefvorlageName != null) {
                    yield tl.translate("documentqueue.massenbrief.displayname", "briefvorlageName", briefvorlageName);
                }
                yield "-";
            }
            case FileTyp.PLAYGROUND -> tl.translate("documentqueue.playground.displayname");
            case FileTyp.PLAYGROUND_ERROR -> tl.translate("documentqueue.playground-error.displayname");
            default -> "-";
        };
    }

    private void throwExceptionIfUserHasToManyQueuedItems() {
        // check if current user has not too many queued documents already
        var queuedDocumentsOfUser = documentQueueService.findAllUnfinishedByCurrentBenutzer();
        if (queuedDocumentsOfUser.size() >= configService.getDocumentqueueMaxOpenPerUser()) {
            throw new AppValidationException(
                AppValidationMessage.tooManyQueuedDocuments(configService.getDocumentqueueMaxOpenPerUser()));
        }
    }
}
