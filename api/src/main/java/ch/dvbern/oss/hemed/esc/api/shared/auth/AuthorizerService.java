package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
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
    private final KindService kindService;
    private final ArchivdokumentService archivdokumentService;

    public void requireWriteAuthorizationAnlass(@NonNull ID<Anlass> anlassId) {
        Anlass anlass = anlassService.getById(anlassId);
        if (!hasWriteAuthorizationAnlass(anlass)) {
            throwViolation(anlass);
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

    public void requireReadAuthorizationAnlass(@NonNull ID<Anlass> anlassId) {
        Anlass anlass = anlassService.getById(anlassId);
        if (!hasReadAuthorizationAnlass(anlass)) {
            throwViolation(anlass);
        }
    }

    public boolean hasReadAuthorizationAnlassListe(@NonNull Anlass anlass) {
        return hasReadAuthorizationAnlassListe(anlass.getAnlassConfig().getAnlassTyp());
    }

    public boolean hasReadAuthorizationAnlassListe(@NonNull AnlassTyp anlassTyp) {
        if (anlassTyp == AnlassTyp.LAEUSEKONTROLLE) {
            return benutzerService.hasAnyOfCallerBerechtigungen(List.of(
                    Berechtigung.ANLASSLISTE_LESEN,
                    Berechtigung.ANLASSLISTE_LESEN_LAEUSE
                )
            );
        }
        return benutzerService.hasCallerBerechtigung(Berechtigung.ANLASSLISTE_LESEN);

    }

    public boolean hasReadAuthorizationAnlass(@NonNull Anlass anlass) {
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            return benutzerService.hasAnyOfCallerBerechtigungen(List.of(
                    Berechtigung.ANLASSPLANUNG_LESEN,
                    Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE
                )
            );
        }
        return benutzerService.hasCallerBerechtigung(Berechtigung.ANLASSPLANUNG_LESEN);
    }

    public boolean hasWriteAuthorizationAnlass(@NonNull Anlass anlass) {
        if (anlass.getAnlassStatus() == AnlassStatus.ABGESCHLOSSEN) {
            return benutzerService.hasCallerBerechtigung(Berechtigung.VERANDERUNGEN_NACH_ABSCHLUSS);
        }
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            return benutzerService.hasAnyOfCallerBerechtigungen(List.of(
                Berechtigung.ANLASSPLANUNG_SCHREIBEN,
                Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE)
            );
        }
        return benutzerService.hasCallerBerechtigung(Berechtigung.ANLASSPLANUNG_SCHREIBEN);
    }

    public void requireKinderDownloadAuthorization(@NonNull ID<Kind> kindID) {
        Kind kind = kindService.getById(kindID);

        if (!benutzerService.hasCallerBerechtigungen(List.of(
            Berechtigung.KINDER_LESEN_MEDIZINISCH,
            Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH, Berechtigung.KOMPLETTER_EXPORT))) {
            throwViolation(kind);
        }
    }

    public void requireArchivDownloadAuthorization(@NonNull ID<Archivdokument> archivDokumentID) {
        Archivdokument archivdokument = archivdokumentService.getById(archivDokumentID);
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        if (!benutzerService.hasCallerBerechtigung(Berechtigung.ARCHIVIERUNG)
            || !(archivdokument.getOwner().equals(currentBenutzer)
                || currentBenutzer.getFachRollen().contains(FachRolle.SUPPORT)
            )
        ) {
            throwViolation(archivdokument);
        }
    }

    private void throwViolation(@NonNull AbstractEntity<?> abstractEntity) {
        String benutzername = benutzerService.findCurrentBenutzerOptional()
            .map(Benutzer::getEmail)
            .map(EmailAddress::getAddress)
            .orElse("unknown");
        throw new AppFailureException(AppFailureMessage.missingBerechtigung(
            abstractEntity.getClass(),
            abstractEntity.getId(),
            benutzername));
    }
}
