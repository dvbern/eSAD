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
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
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
    private final DocumentQueueService documentQueueService;
    private final KindService kindService;
    private final ArchivdokumentService archivdokumentService;
    private final TenantProducer tenantProducer;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final EinladungService einladungService;
    private final DossierService dossierService;

    // ORGANISATION

    public void requireReadAuthorizationOrganisation(@NonNull ID<Organisation> organisationID) {
        Organisation organisation = organisationService.getById(organisationID);
        if (!hasReadAuthorizationOrganisation(organisation)) {
            throwViolation(organisation);
        }
    }

    public boolean hasReadAuthorizationOrganisation(@NonNull Organisation organisation) {
        return isInOrganisationWithBerechtigung(organisation, List.of(Berechtigung.ORGANISATION_VERWALTEN_LESEN), null);
    }

    // BENUTZER

    public void requireWriteAuthorizationBenutzer(
        @NonNull Benutzer benutzer,
        @NonNull List<Berechtigung> berechtigungenAnnotated
    ) {
        Organisation organisation = benutzer.getSingleOrganisation();
        if (!isInOrganisationWithBerechtigung(organisation, berechtigungenAnnotated, null)) {
            throwViolation(benutzer);
        }
    }

    // ANLASS

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

    public void requireReadAuthorizationAnlassBasic(@NonNull ID<Anlass> anlassId, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        Anlass anlass = anlassService.getById(anlassId);
        if (!hasReadAuthorizationAnlassBasic(anlass, berechtigungenAnnotated)) {
            throwViolation(anlass);
        }
    }

    public boolean hasReadAuthorizationAnlassBasic(@NonNull Anlass anlass, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            List<Berechtigung> additionalBerechtigungenLaeuse =
                List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE);
            boolean anlassZustaendig = isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, additionalBerechtigungenLaeuse);
            if (anlassZustaendig) {
                return true;
            }
            List<Einladung> einladungen = einladungService.findByAnlass(anlass.getId());
            for (Einladung einladung : einladungen) {
                if (hasReadAuthorizationEinladung(einladung, berechtigungenAnnotated, additionalBerechtigungenLaeuse)) {
                    return true;
                }
            }
        } else {
            boolean anlassZustaendig = isZustaendigWithBerechtigung(
                anlass,
                berechtigungenAnnotated,
                List.of(Berechtigung.ANLASSPLANUNG_LESEN));
            if (anlassZustaendig) {
                return true;
            }
            List<Einladung> einladungen = einladungService.findByAnlass(anlass.getId());
            for (Einladung einladung : einladungen) {
                if (hasReadAuthorizationEinladung(
                    einladung,
                    berechtigungenAnnotated,
                    List.of(Berechtigung.ANLASSPLANUNG_LESEN))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasReadAuthorizationAnlass(@NonNull Anlass anlass, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (anlass.getAnlassConfig().getAnlassTyp() == AnlassTyp.LAEUSEKONTROLLE) {
            return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE));
        }
        return isZustaendigWithBerechtigung(anlass, berechtigungenAnnotated, List.of(Berechtigung.ANLASSPLANUNG_LESEN));
    }

    // EINLADUNG

    public void requireWriteAuthorizationEinladung(@NonNull Einladung einladung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationEinladung(einladung, berechtigungenAnnotated)) {
            throwViolation(einladung);
        }
    }

    public boolean hasWriteAuthorizationEinladung(@NonNull Einladung einladung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Aktuell gleich wie Anlass
        return hasWriteAuthorizationAnlass(einladung.getAnlass(), berechtigungenAnnotated);
    }

    public void requireReadAuthorizationEinladung(
        @NonNull Einladung einladung,
        @NonNull List<Berechtigung> berechtigungenAnnotated
    ) {
        if (!hasReadAuthorizationEinladung(einladung, berechtigungenAnnotated,null)) {
            throwViolation(einladung);
        }
    }

    @SuppressWarnings("java:S1126") // readability is better with seprate return
    public boolean hasReadAuthorizationEinladung(
        @NonNull Einladung einladung,
        @NonNull List<Berechtigung> berechtigungenAnnotated,
        @Nullable List<Berechtigung> additionalBerechtigungen
    ) {
        // Schreiben duerfen nur die Zustaendigen des Anlasses (-> hasWriteAuthorizationAnlass)
        // Lesen duerfen die Zustaendigen des Anlasses, des Dossiers oder der Schule
        if (isZustaendigWithBerechtigung(einladung.getAnlass(), berechtigungenAnnotated, additionalBerechtigungen)) {
            return true;
        }
        if (isZustaendigWithBerechtigung(einladung.getDossier(), berechtigungenAnnotated, additionalBerechtigungen)) {
            return true;
        }
        Schule schule = einladung.getDossier().getKind().getSchule();
        if (schule != null && isZustaendigWithBerechtigung(schule, berechtigungenAnnotated, additionalBerechtigungen)) {
            return true;
        }
        return false;
    }

    // UNTERSUCHUNG

    public void requireWriteAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationUntersuchung(untersuchung, berechtigungenAnnotated)) {
            throwViolation(untersuchung);
        }
    }

    public boolean hasWriteAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Aktuell gleich wie Anlass
        return hasWriteAuthorizationAnlass(untersuchung.getAnlass(), berechtigungenAnnotated);
    }

    public void requireReadAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasReadAuthorizationUntersuchung(untersuchung, berechtigungenAnnotated)) {
            throwViolation(untersuchung);
        }
    }

    public boolean hasReadAuthorizationUntersuchung(@NonNull Untersuchung untersuchung, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (untersuchung.getEinladungOptional().isPresent()) {
            return hasReadAuthorizationEinladung(untersuchung.getEinladungRequired(), berechtigungenAnnotated, null);
        }
        // Fuer AnonymeEintraege ist nur der Anlass-Zustaendige berechtigt
        return hasReadAuthorizationAnlass(untersuchung.getAnlass(), berechtigungenAnnotated);
    }

    // DOSSIER

    public void requireWriteAuthorizationDossier(@NonNull Dossier dossier, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationDossier(dossier, berechtigungenAnnotated, null)) {
            throwViolation(dossier);
        }
    }

    @SuppressWarnings("java:S1126") // readability is better with seprate return
    private boolean hasWriteAuthorizationDossier(@NonNull Dossier dossier, @NonNull List<Berechtigung> berechtigungenAnnotated, @Nullable List<Berechtigung> additionalBerechtigungen) {
        // Dossier darf vom Dossier-Zuständigen und Schul-Zuständigen editiert werden (aber nicht vom Anlass-Zuständigen)
        if (isZustaendigWithBerechtigung(dossier, berechtigungenAnnotated, additionalBerechtigungen)) {
            return true;
        }
        Schule schule = dossier.getKind().getSchule();
        if (schule != null && isZustaendigWithBerechtigung(schule, berechtigungenAnnotated, additionalBerechtigungen)) {
            return true;
        }
        return false;
    }

    // AUFGABE

    public void requireWriteAuthorizationAufgabe(@NonNull Aufgabe aufgabe, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationAufgabe(aufgabe, berechtigungenAnnotated)) {
            throwViolation(aufgabe);
        }
    }

    public boolean hasWriteAuthorizationAufgabe(@NonNull Aufgabe aufgabe, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Aktuell gleich wie Lesen
        return hasReadAuthorizationAufgabe(aufgabe, berechtigungenAnnotated);
    }

    public void requireReadAuthorizationAufgabe(@NonNull Aufgabe aufgabe, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasReadAuthorizationAufgabe(aufgabe, berechtigungenAnnotated)) {
            throwViolation(aufgabe);
        }
    }

    public boolean hasReadAuthorizationAufgabe(@NonNull Aufgabe aufgabe, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        boolean isZentraleOrganisation = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getZentraleOrganisationFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (isZentraleOrganisation) {
            return isInOrganisationWithBerechtigung(
                organisationService.getZentraleOrganisation(),
                berechtigungenAnnotated,
                null);
        }

        Benutzer benutzerAufgabe = aufgabe.getEmpfaenger();
        Organisation organisationOrNull = benutzerAufgabe.getSingleOrganisation();

        return isInOrganisationWithBerechtigung(
            organisationOrNull,
            berechtigungenAnnotated,
            null);
    }

    public boolean hasReadAuthorizationDossier(@NonNull Dossier dossier, @NonNull List<Berechtigung> berechtigungenAnnotated, @Nullable List<Berechtigung> additionalBerechtigungen) {
        // Schreiben und Lesen nach heutiger Sicht genau gleich
        return hasWriteAuthorizationDossier(dossier, berechtigungenAnnotated, additionalBerechtigungen);
    }

    // DUPLIKAT

    public void requireWriteAuthorizationDuplikat(@NonNull Duplikat duplikat, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationDuplikat(duplikat, berechtigungenAnnotated)) {
            throwViolation(duplikat);
        }
    }

    public boolean hasWriteAuthorizationDuplikat(@NonNull Duplikat duplikat, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Duplikat ist editierbar, wenn es noch kein Dossier hat. Wenn es ein Dossier hat, muss ich fuer dieses berechtigt sein
        if (duplikat.getKind1() != null) {
            Optional<Dossier> dossierOptional1 = dossierService.findByKind(duplikat.getKind1().getId());
            if (dossierOptional1.isPresent() && !hasWriteAuthorizationDossier(dossierOptional1.get(), berechtigungenAnnotated, null)) {
                return false;
            }
        }
        if (duplikat.getKind2() != null) {
            Optional<Dossier> dossierOptional2 = dossierService.findByKind(duplikat.getKind2().getId());
            if (dossierOptional2.isPresent() && !hasWriteAuthorizationDossier(dossierOptional2.get(), berechtigungenAnnotated, null)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasReadAuthorizationDuplikat(@NonNull Duplikat duplikat, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        // Schreiben und Lesen nach heutiger Sicht genau gleich
        return hasWriteAuthorizationDuplikat(duplikat, berechtigungenAnnotated);
    }

    // NOTIZ

    private boolean hasWriteAuthorizationNotiz(@NonNull Notiz notiz, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!notiz.hasLabel(Label.ID_LABEL_DISPENSATIONSGESUCH)) {
            boolean result = hasWriteAuthorizationDossier(notiz.getDossier(), berechtigungenAnnotated, List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN));
            if (result) {
                return true;
            }
            return isInOrganisationWithBerechtigung(notiz.getVerantwortlicher().getSingleOrganisation(), berechtigungenAnnotated,  List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN));
        }
        boolean result = hasWriteAuthorizationDossier(notiz.getDossier(), berechtigungenAnnotated, null);
        if (result) {
            return true;
        }
        return isInOrganisationWithBerechtigung(notiz.getVerantwortlicher().getSingleOrganisation(), berechtigungenAnnotated, null);
    }

    public boolean hasReadAuthorizationNotiz(@NonNull Notiz notiz, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!notiz.hasLabel(Label.ID_LABEL_DISPENSATIONSGESUCH)) {
            boolean result = hasReadAuthorizationDossier(notiz.getDossier(), berechtigungenAnnotated, List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN));
            if (result) {
                return true;
            }
            return isInOrganisationWithBerechtigung(notiz.getVerantwortlicher().getSingleOrganisation(), berechtigungenAnnotated, List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN));
        }
        boolean result = hasReadAuthorizationDossier(notiz.getDossier(), berechtigungenAnnotated, null);
        if (result) {
            return true;
        }
        return isInOrganisationWithBerechtigung(notiz.getVerantwortlicher().getSingleOrganisation(), berechtigungenAnnotated, null);
    }

    public void requireWriteAuthorizationNotiz(@NonNull Notiz notiz, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasWriteAuthorizationNotiz(notiz, berechtigungenAnnotated)) {
            throwViolation(notiz);
        }
    }

    public void requireReadAuthorizationNotiz(@NonNull Notiz notiz, @NonNull List<Berechtigung> berechtigungenAnnotated) {
        if (!hasReadAuthorizationNotiz(notiz, berechtigungenAnnotated)) {
            throwViolation(notiz);
        }
    }

    // WEITERE

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
        // Bewusst findByKind statt getOrCreateByKind: ein Authorization-Check darf keine Daten anlegen.
        Dossier dossier = dossierService.findByKind(kindID)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.missingBerechtigung(
                Kind.class,
                kindID,
                benutzerService.findCurrentBenutzerOptional()
                    .map(Benutzer::getEmail)
                    .map(EmailAddress::getAddress)
                    .orElse("unknown"))));
        if (!hasReadAuthorizationDossier(dossier, List.of(
            Berechtigung.KINDER_LESEN_MEDIZINISCH, Berechtigung.KINDER_LESEN_NICHT_MEDIZINISCH, Berechtigung.KOMPLETTER_EXPORT), null)) {
            throwViolation(kind);
        }
    }

    public void requireArchivDownloadAuthorization(@NonNull ID<Archivdokument> archivDokumentID) {
        Archivdokument archivdokument = archivdokumentService.getById(archivDokumentID);
        Organisation organisationArchivOwner = archivdokument.getOwner().getSingleOrganisation();

        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();

        if (!isInOrganisationWithBerechtigung(organisationArchivOwner, List.of(Berechtigung.ARCHIVIERUNG), null)
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
