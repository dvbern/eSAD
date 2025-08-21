package ch.dvbern.oss.hemed.esc.api.shared.auth;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequestScoped
@Slf4j
@RequiredArgsConstructor
public class AuthorizerService {

    private final BenutzerService benutzerService;
    private final AnlassService anlassService;
    private final NotizService notizService;
    private final DocumentQueueService documentQueueService;

    public void requireWriteAuthorizationAnlass(@NonNull ID<Anlass> anlassId) {
        Anlass anlass = anlassService.getById(anlassId);
        if (anlass.getAnlassStatus() == AnlassStatus.ABGESCHLOSSEN) {
            if (!benutzerService.hasCallerBerechtigung(Berechtigung.VERANDERUNGEN_NACH_ABSCHLUSS)) {
                throwViolation(anlass);
            }
        }
    }

    public boolean hasWriteAuthorizationNotiz(@NonNull Notiz notiz) {
        if (!notiz.hasLabel(Label.ID_LABEL_DISPENSATIONSGESUCH)) {
            return benutzerService.hasCallerBerechtigung(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN);
        }
        return true;
    }

    public boolean hasReadAuthorizationNotiz(@NonNull Notiz notiz) {
        if (!notiz.hasLabel(Label.ID_LABEL_DISPENSATIONSGESUCH)) {
            return benutzerService.hasCallerBerechtigung(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN);
        }
        return true;
    }

    public void requireWriteAuthorizationNotiz(@NonNull ID<Notiz> notizId) {
        Notiz notiz = notizService.getById(notizId);
        if (!hasWriteAuthorizationNotiz(notiz)) {
            throwViolation(notiz);
        }
    }

    public void requireReadAuthorizationNotiz(@NonNull ID<Notiz> notizId) {
        Notiz notiz = notizService.getById(notizId);
        if (!hasReadAuthorizationNotiz(notiz)) {
            throwViolation(notiz);
        }
    }

    public void requireReadAuthorizationDocument(@NonNull ID<DocumentQueue> documentQueueId) {
        DocumentQueue documentQueue = documentQueueService.getById(documentQueueId);
        if (!checkReadAuthorizationDocument(documentQueue)) {
            throwViolation(documentQueue);
        }
    }

    public boolean checkReadAuthorizationDocument(@NonNull ID<DocumentQueue> documentQueueId) {
        DocumentQueue documentQueue = documentQueueService.getById(documentQueueId);
        return checkReadAuthorizationDocument(documentQueue);
    }

    public boolean checkReadAuthorizationDocument(@NonNull DocumentQueue documentQueue) {
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        return documentQueue.getBenutzer().equals(currentBenutzer);
    }

    private void throwViolation(@NonNull AbstractEntity<?> abstractEntity) {
        String benutzername = benutzerService.findCurrentBenutzerOptional().map(Benutzer::getEmail).map(EmailAddress::getAddress).orElse("unknown");
        throw new AppFailureException(AppFailureMessage.missingBerechtigung(
            abstractEntity.getClass(),
            abstractEntity.getId(),
            benutzername));
    }
}
