package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequestScoped
@Slf4j
@RequiredArgsConstructor
public class AuthorizerService {

    private final BenutzerService benutzerService;
    private final OrganisationService organisationService;
    private final AnlassService anlassService;
    private final NotizService notizService;
    private final DocumentQueueService documentQueueService;
    private final KindService kindService;
    private final ArchivdokumentService archivdokumentService;
    private final TenantProducer tenantProducer;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;

    public void requireReadAuthorizationOrganisation(@NonNull ID<Organisation> organisationID) {
        Organisation organisation = organisationService.getById(organisationID);
        if (!hasReadAuthorizationOrganisation(organisation)) {
            throwViolation(organisation);
        }
    }

    public boolean hasReadAuthorizationOrganisation(@NonNull Organisation organisation) {
        return isInOrganisationWithBerechtigung(organisation, List.of(Berechtigung.ORGANISATION_VERWALTEN_LESEN), null);
    }

    public void requireWriteAuthorizationAnlass(@NonNull ID<Anlass> anlassId, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        Anlass anlass = anlassService.getById(anlassId);
        if (!hasWriteAuthorizationAnlass(anlass, berechtigungenAnnotated)) {
            throwViolation(anlass);
        }
    }

    public boolean hasWriteAuthorizationAnlass(@NonNull Anlass anlass, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (anlass.getAnlassStatus() == AnlassStatus.ABGESCHLOSSEN) {
            return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.VERANDERUNGEN_NACH_ABSCHLUSS));
        }
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE));
        }
        return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN));
    }

    public void requireReadAuthorizationAnlass(@NonNull ID<Anlass> anlassId, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        Anlass anlass = anlassService.getById(anlassId);
        if (!hasReadAuthorizationAnlass(anlass, berechtigungenAnnotated)) {
            throwViolation(anlass);
        }
    }

    public boolean hasReadAuthorizationAnlass(@NonNull ID<Anlass> anlassID, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        Anlass anlass = anlassService.getById(anlassID);
        return hasReadAuthorizationAnlass(anlass, berechtigungenAnnotated);
    }

    public boolean hasReadAuthorizationAnlass(@NonNull Anlass anlass, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE));
        }
        return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_LESEN));
    }

    public void requireWriteAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationUntersuchung(untersuchung, berechtigungenAnnotated)) {
            throwViolation(untersuchung);
        }
    }

    public boolean hasWriteAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Aktuell gleich wie Anlass
        return hasWriteAuthorizationAnlass(untersuchung.getAnlass(), berechtigungenAnnotated);
    }

    public void requireReadAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull Berechtigung... berechtigungenAnnotated) {
        if (!hasReadAuthorizationUntersuchung(untersuchung, berechtigungenAnnotated)) {
            throwViolation(untersuchung);
        }
    }

    @SuppressWarnings("java:S1126") // readability is better with seprate return
    public boolean hasReadAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull Berechtigung... berechtigungenAnnotated) {
        // Schreiben duerfen nur die Zustaendigen des Anlasses (-> hasWriteAuthorizationAnlass)
        // Lesen duerfen die Zustaendigen des Anlasses, des Dossiers oder der Schule
        if (isZustaendigWithBerechtigung(untersuchung.getAnlass(), List.of(berechtigungenAnnotated), null)) {
            return true;
        }
        if (untersuchung.getEinladungOptional().isPresent()) {
            if (isZustaendigWithBerechtigung(untersuchung.getEinladungRequired().getDossier(), List.of(berechtigungenAnnotated), null)) {
                return true;
            }
            Schule schule = untersuchung.getEinladungRequired().getDossier().getKind().getSchule();
            if (schule != null && isZustaendigWithBerechtigung(schule, List.of(berechtigungenAnnotated), null)) {
                return true;
            }
        }
        return false;
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
                || currentBenutzer.hasFachRolleSupport()
            )
        ) {
            throwViolation(archivdokument);
        }
    }

    private boolean isZustaendigWithBerechtigung(
        @NonNull HasZustaendigkeit entityWithZustaendigkeit,
        @NonNull List<Berechtigung> berechtigungenAnnotated,
        @Nullable List<Berechtigung> additionalBerechtigungen
    ) {
        boolean isZentraleOrganisation = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getZentraleOrganisationFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
        if (isZentraleOrganisation) {
            return isInOrganisationWithBerechtigung(organisationService.getZentraleOrganisation(), berechtigungenAnnotated, additionalBerechtigungen);
        }
        if (entityWithZustaendigkeit.getZustaendigArzt() == null) {
            return false;
        }
        Organisation organisationOrNull = entityWithZustaendigkeit.getSingleOrganisation();
        return isInOrganisationWithBerechtigung(
            organisationOrNull,
            berechtigungenAnnotated,
            additionalBerechtigungen);
    }

    private boolean isInOrganisationWithBerechtigung(
        @Nullable Organisation organisationOrNull,
        @NonNull List<Berechtigung> berechtigungenAnnotated,
        @Nullable List<Berechtigung> additionalBerechtigungen
    ) {
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        TenantEnum tenant = tenantProducer.produceTenant();

        boolean hasSystemBerechtigung = Optional.ofNullable(currentBenutzer.getSystemRollen())
            .map(rollen -> rollen.getBerechtigungen(tenant))
            .map(berechtigungen -> hasRequiredBerechtigungen(berechtigungen, berechtigungenAnnotated, additionalBerechtigungen))
            .orElse(false);
        if (hasSystemBerechtigung) {
            return true;
        }
        if (organisationOrNull == null) {
            return false;
        }
        return Optional.ofNullable(currentBenutzer.getSingleOrganisationRollen())
            .filter(rolle -> rolle.getOrganisation().getId().equals(organisationOrNull.getId()))
            .map(rolle -> rolle.getBerechtigungen(tenant))
            .map(berechtigungen -> hasRequiredBerechtigungen(berechtigungen, berechtigungenAnnotated, additionalBerechtigungen))
            .orElse(false);
    }

    private boolean hasRequiredBerechtigungen(
        @NonNull Set<Berechtigung> hasBerechtigungen,
        @NonNull List<Berechtigung> requiredAnnotated,
        @Nullable List<Berechtigung> additionalRequired
    ) {
        boolean hasAnnotated = requiredAnnotated.stream().anyMatch(hasBerechtigungen::contains);

        if (!hasAnnotated) {
            return false;
        }

        if (additionalRequired != null) {
            return additionalRequired.stream().anyMatch(hasBerechtigungen::contains);
        }

        return true;
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
