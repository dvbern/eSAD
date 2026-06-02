package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueTyp;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import ch.dvbern.oss.hemed.esc.api.shared.util.VerantwortlicherSchuleChangedDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZustaendigePersonService {

    private final BenutzerService benutzerService;
    private final DossierService dossierService;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final ArchivierungService archivierungService;
    private final ObjectMapper objectMapper;
    private final DossierQueueService dossierQueueService;
    private final EinladungService einladungService;
    private final AnlassService anlassService;


    public void requireZustaendigkeit(@NonNull HasZustaendigkeit entity) {
        // Zustaendigkeit darf nur vom aktuell Zuständigen geändert werden
        Organisation currentOrganisationOrNull = entity.getSingleOrganisation();
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        boolean hasSystemBerechtigung = currentBenutzer.getSystemRollen() != null;
        if (hasSystemBerechtigung) {
            return;
        }
        if (currentOrganisationOrNull == null) {
            return;
        }
        boolean hasOrganisationBerechtigung = Optional.ofNullable(currentBenutzer.getSingleOrganisationRollen())
            .filter(rolle -> rolle.getOrganisation().getId().equals(currentOrganisationOrNull.getId()))
            .isPresent();
        if (!hasOrganisationBerechtigung) {
            throw new AppValidationException(AppValidationMessage.zustaendigkeitCanOnlyBeChangedByZustaendiger());
        }
    }

    public boolean hasZustaendigkeitChanged(@NonNull HasZustaendigkeit entity, @Nullable ID<Benutzer> newZustaendigerArztID) {
        if (entity.getZustaendigArzt() != null && !entity.getZustaendigArzt().getId().equals(newZustaendigerArztID)) {
            Organisation currentOrganisationOrNull = entity.getSingleOrganisation();
            Organisation newOrganisationOrNull = newZustaendigerArztID != null ? benutzerService.getById(newZustaendigerArztID).getSingleOrganisation() : null;
            return currentOrganisationOrNull != null && !currentOrganisationOrNull.equals(newOrganisationOrNull);
        }
        return false;
    }

    public void verantwortlicherChangedOnSchuleAsync(
        @NonNull Schule schule,
        @NonNull VerantwortlicherSchuleChangedDTO args
    ) {
        if (args.hasArztChanged() || args.hasMpaChanged()) {
            try {
                String jsonString = objectMapper.writeValueAsString(args);
                dossierService.findBySchule(schule.getId()).forEach(dossier
                    -> dossierQueueService.createDossierQueueItem(
                        dossier.getId(),
                        DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE,
                        jsonString
                    ));
            } catch (JsonProcessingException e) {
                throw new AppFailureException(
                    AppFailureMessage.internalError("Could not store json parameter for VERANTWORTLICHER_CHANGED_ON_SCHULE"), e);
            }
        }
    }

    // TODO ESC-1222 Aufräumtask wegen Organisationen
    @Transactional(TxType.REQUIRES_NEW)
    public void verantwortlicherChangedOnSchule(
        @NonNull ID<Dossier> dossierID,
        @Nullable String parameters
    ) {
        try {
            Dossier dossier = dossierService.getById(dossierID);
            VerantwortlicherSchuleChangedDTO args = objectMapper.readValue(parameters, VerantwortlicherSchuleChangedDTO.class);
            // TODO ESC-1139 Soll bei VerantwortlicherChange on Schule nicht archiviert werden?
            handleSchulVerantwortlicheChangedForDossier(dossier, args, false);
        } catch (JsonProcessingException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not read json parameter for VERANTWORTLICHER_CHANGED_ON_SCHULE"), e);
        }
    }

    public void schuleChangedOnDossier(
        @NonNull Dossier dossier,
        @NonNull VerantwortlicherSchuleChangedDTO args
    ) {
        handleSchulVerantwortlicheChangedForDossier(dossier, args, true);
    }

    // TODO ESC-1222 Aufräumtask wegen Organisationen
    private void handleSchulVerantwortlicheChangedForDossier(
        @NonNull Dossier dossier,
        @NonNull VerantwortlicherSchuleChangedDTO args,
        boolean archivierenIfNecessary
    ) {
        FeatureEnabledSetting zentraleOrganisationFeature =
            tenantSettingsService.getTenantSetting().getTenantSettingFeatures().getZentraleOrganisationFeature();
        boolean zentraleOrganisation =
            zentraleOrganisationFeature.isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (zentraleOrganisation) {
            // Bei zentral organisierten Tenants gilt das Prinzip der letzten Änderung. D.h. egal ob vorher
            // die Zuständigkeiten auf dem Dossier überschrieben waren, die neuere Änderung ist auf der Schule
            // und dies gilt auch für das Dossier (null bei zentralen bedeutet gleich wie Schule)
            if (dossier.getZustaendigArzt() != null) {
                dossier.setZustaendigArzt(null);
            }
            if (dossier.getZustaendigMPA() != null) {
                dossier.setZustaendigMPA(null);
            }
            dossier.disableOptimisticLockingCheck();
            dossierService.persist(dossier);
        } else {
            // Bei dezentral organisierten Tenants wird der Schule-Arzt uebernommen, falls vorher Schule und
            // Dossier die gleichen Verantwortlichen hatten
            if (args.hasArztChanged()) {
                ID<Benutzer> arztFromDossierID = dossier.getZustaendigArzt() != null ? dossier.getZustaendigArzt().getId() : null;
                if (Objects.equals(arztFromDossierID, args.getArztVorher())) {
                    if (args.getArztNachher() != null) {
                        // Archivieren fuer den bisherigen Verantwortlichen des Dossiers
                        if (archivierenIfNecessary) {
                            // Nur wenn wir tatsächlich den Verantwortlichen geändert haben, muss u.U. archiviert werden
                            if (arztFromDossierID != null) {
                                Organisation orgsNew = benutzerService.getById(args.getArztNachher()).getSingleOrganisation();
                                Organisation orgsOld = benutzerService.getById(arztFromDossierID).getSingleOrganisation();
                                if (!Objects.equals(orgsNew, orgsOld)) {
                                    // Der bisherige Arzt ist nicht mehr berechtigt -> archivieren
                                    Benutzer arztFuerArchivierung = benutzerService.getById(arztFromDossierID);
                                    archivierungService.createGesamtArchivdokument(dossier, arztFuerArchivierung, ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED);
                                }
                            }
                        }
                        // Den neuen Verantwortlichen auf dem Dossier setzen
                        Benutzer arztFromSchuleNew = benutzerService.getBenutzerIfCorrectRole(args.getArztNachher(), FachRolle.SCHULAERZTIN);
                        dossier.setZustaendigArzt(arztFromSchuleNew);
                        dossier.disableOptimisticLockingCheck();
                        dossierService.persist(dossier);
                    }
                }
            }
            if (args.hasMpaChanged()) {
                ID<Benutzer> mpaFromDossierID = dossier.getZustaendigMPA() != null ? dossier.getZustaendigMPA().getId() : null;
                if (Objects.equals(mpaFromDossierID, args.getMpaVorher())) {
                    if (args.getMpaNachher() != null) {
                        Benutzer mpaFromSchuleNew = benutzerService.getBenutzerIfOneOfRolesIsCorrect(args.getMpaNachher(), FachRolle.fachrollenHilfsperson());
                        dossier.setZustaendigMPA(mpaFromSchuleNew);
                        dossier.disableOptimisticLockingCheck();
                        dossierService.persist(dossier);
                    }
                }
            }
        }
    }

    @NonNull
    public Dossier verantwortlicherChangedOnDossier(
        @NonNull Dossier dossier,
        @Nullable ID<Benutzer> arztDossierNeu,
        @Nullable ID<Benutzer> mpaDossierNeu
    ) {
        dossier.disableOptimisticLockingCheck();
        FeatureEnabledSetting zentraleOrganisationFeature =
            tenantSettingsService.getTenantSetting().getTenantSettingFeatures().getZentraleOrganisationFeature();
        boolean zentraleOrganisation =
            zentraleOrganisationFeature.isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (zentraleOrganisation) {
            verantwortlicherChangedOnDossierZentraleOrganisation(dossier, arztDossierNeu, mpaDossierNeu);

        } else {
            verantwortlicherChangeOnDossierDezentraleOrganisation(dossier, arztDossierNeu, mpaDossierNeu);
        }
        return dossierService.persist(dossier);
    }

    private void verantwortlicherChangedOnDossierZentraleOrganisation(
        @NonNull Dossier dossier,
        @Nullable ID<Benutzer> arztDossierNeu,
        @Nullable ID<Benutzer> mpaDossierNeu
    ) {
        ID<Benutzer> mpaFromSchule = dossier.getZustaendigMpaIdFromSchule();
        if (Objects.equals(mpaFromSchule, mpaDossierNeu)) {
            dossier.setZustaendigMPA(null);
        } else {
            dossier.setZustaendigMPA(benutzerService.getBenutzerIfOneOfRolesIsCorrect(mpaDossierNeu, FachRolle.fachrollenHilfsperson()));
        }
        // Bei zentrale organisierten Tenants werden die Verantwortlichen nur gesetzt, wenn sie nicht sowieso
        // schon auf der Schule gesetzt sind
        ID<Benutzer> arztFromSchule = dossier.getZustaendigArztIdFromSchule();
        if (Objects.equals(arztFromSchule, arztDossierNeu)) {
            dossier.setZustaendigArzt(null);
        } else {
            dossier.setZustaendigArzt(benutzerService.getBenutzerIfCorrectRole(arztDossierNeu, FachRolle.SCHULAERZTIN));
        }
    }

    private void verantwortlicherChangeOnDossierDezentraleOrganisation(
        @NonNull Dossier dossier,
        @Nullable ID<Benutzer> arztDossierNeu,
        @Nullable ID<Benutzer> mpaDossierNeu
    ) {
        ID<Benutzer> mpaDossierAlt = dossier.getZustaendigMPA() != null ? dossier.getZustaendigMPA().getId() : null;
        if (!Objects.equals(mpaDossierAlt, mpaDossierNeu)) {
            // Hilfsperson hat geändert
            dossier.setZustaendigMPA(benutzerService.getBenutzerIfOneOfRolesIsCorrect(mpaDossierNeu, FachRolle.fachrollenHilfsperson()));
        }
        // Bei dezentral organisierten Tenants wird der neue Arzt immer übernommen (wenn er überhaupt geändert hat)
        // Falls dadurch der bisherige Arzt den Zugriff verliert, erhält er eine Archivierung
        Benutzer arztDossierAlt = dossier.getZustaendigArzt();
        ID<Benutzer> arztDossierAltID = arztDossierAlt != null ? arztDossierAlt.getId() : null;
        if (!Objects.equals(arztDossierAltID, arztDossierNeu)) {
            // Arzt hat geändert
            dossier.setZustaendigArzt(benutzerService.getBenutzerIfCorrectRole(arztDossierNeu, FachRolle.SCHULAERZTIN));
            // War vorher ein Arzt gesetzt und hat dieser durch die Änderung den Zugriff verloren?
            if (arztDossierAlt != null && !dossier.isBenutzerZustaendigForDossier(arztDossierAlt)) {
                archivierungService.createGesamtArchivdokument(dossier, arztDossierAlt, ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED);
            }
        }
    }

    @NonNull
    public Anlass verantwortlicherChangedOnAnlass(
        @NonNull Anlass anlass,
        @Nullable ID<Benutzer> arztAnlassNeu,
        @Nullable ID<Benutzer> mpaAnlassNeu
    ) {
        // Sollte nur gemacht werden wenn der Arzt auch wirklich gewechselt hat, ansonsten wird für einen Privatarzt bei jeder Änderung am Anlass die Archivierung durchgeführt.
        if (anlass.getArzt() != null && !anlass.getArzt().getId().equals(arztAnlassNeu)) {
            List<Einladung> einladungen = einladungService.findByAnlass(anlass.getId());
            for (Einladung einladung : einladungen) {
                verantwortlicherChangedOnAnlass(anlass, einladung.getDossier());
            }
        }

        anlass.setArzt(benutzerService.getBenutzerIfCorrectRole(arztAnlassNeu, FachRolle.SCHULAERZTIN));
        anlass.setMpa(benutzerService.getBenutzerIfOneOfRolesIsCorrect(mpaAnlassNeu, FachRolle.fachrollenHilfsperson()));
        anlass.disableOptimisticLockingCheck();
        return anlassService.persistAndFlush(anlass);
    }

    private void verantwortlicherChangedOnAnlass(
        @NonNull Anlass anlass,
        @NonNull Dossier dossier
    ) {
        FeatureEnabledSetting zentraleOrganisationFeature =
            tenantSettingsService.getTenantSetting().getTenantSettingFeatures().getZentraleOrganisationFeature();
        boolean zentraleOrganisation =
            zentraleOrganisationFeature.isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (!zentraleOrganisation) {
            // War vorher ein Arzt gesetzt und hat dieser durch die Änderung den Zugriff verloren?
            Benutzer arztAnlassAlt = anlass.getArzt();
            if (arztAnlassAlt != null && !dossier.isBenutzerZustaendigForDossier(arztAnlassAlt)) {
                archivierungService.createTeilArchivdokument(dossier, ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED, anlass);
            }
        }
    }

    public void verantwortlicherChangedByDuplikatMerge(
        @NonNull Dossier basisDossier,
        @NonNull Dossier otherDossier
    ) {
        FeatureEnabledSetting zentraleOrganisationFeature =
            tenantSettingsService.getTenantSetting().getTenantSettingFeatures().getZentraleOrganisationFeature();
        boolean zentraleOrganisation =
            zentraleOrganisationFeature.isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (!zentraleOrganisation && otherDossier.getZustaendigArzt() != null) {
            if (!basisDossier.isBenutzerZustaendigForDossier(otherDossier.getZustaendigArzt())) {
                archivierungService.createGesamtArchivdokument(otherDossier, otherDossier.getZustaendigArzt(), ArchivierungTyp.DUPLIKAT_MERGE);
            }
        }
    }
}
