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
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueCsvSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueDisplayJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueSpssSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class DocumentQueueMapper {

    private final CurrentUserService currentUserService;
    private final DocumentQueueService documentQueueService;
    private final AppLanguageProvider appLanguageProvider;

    private final ObjectMapper objectMapper;
    private final ConfigService configService;

    @NonNull
    public static DocumentQueueDisplayJson toJson(@NonNull DocumentQueue documentQueue) {

        FileData fileData = documentQueue.getResult();

        return new DocumentQueueDisplayJson(
            documentQueue.getId(),
            EntityInfoJson.fromEntity(documentQueue),
            documentQueue.getType(),
            documentQueue.getStatus(),
            documentQueue.getTimestampResult(),
            fileData != null ? fileData.getCalculatedFileName().toString() : null,
            documentQueue.getLastError(),
            documentQueue.getType() == FileTyp.SPSS_REPORT ?
                DocumentQueueService.unmarshalDocumentQueueParameter(documentQueue) :
                null,
            documentQueue.getType() == FileTyp.CSV_REPORT ?
                DocumentQueueService.unmarshalDocumentQueueCsvParameter(documentQueue) :
                null
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
        // check if current user has not too many queued documents already
        var queuedDocumentsOfUser = documentQueueService.findAllUnfinishedByCurrentBenutzer();
        if (queuedDocumentsOfUser.size() >= configService.getDocumentqueueMaxOpenPerUser()) {
            throw new AppValidationException(
                AppValidationMessage.tooManyQueuedDocuments(configService.getDocumentqueueMaxOpenPerUser()));
        }

        DocumentQueue documentQueue = new DocumentQueue();
        documentQueue.setType(saveJson.getType());
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
        // check if current user has not too many queued documents already
        var queuedDocumentsOfUser = documentQueueService.findAllUnfinishedByCurrentBenutzer();
        if (queuedDocumentsOfUser.size() >= configService.getDocumentqueueMaxOpenPerUser()) {
            throw new AppValidationException(
                AppValidationMessage.tooManyQueuedDocuments(configService.getDocumentqueueMaxOpenPerUser()));
        }

        DocumentQueue documentQueue = new DocumentQueue();
        documentQueue.setType(saveJson.getType());
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

    public void removeDocumentQueueItems(@NonNull List<ID<DocumentQueue>> list) {
        List<DocumentQueue> documentsToRemove = list.stream()
            .map(documentQueueService::getById)
            .toList();
        documentQueueService.removeDocumentQueueItems(documentsToRemove);
    }
}
