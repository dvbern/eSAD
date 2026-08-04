package ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschlussService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintragService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparatService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSpracheService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriageService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungenService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungImpfungenUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UntersuchungService {

    private final UntersuchungRepo untersuchungRepo;
    private final EinladungService einladungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungDurchfuehrungService untersuchungDurchfuehrungService;
    private final UntersuchungSehenBeurteilungService untersuchungSehenBeurteilungService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenOtoskopieService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final UntersuchungTriageService untersuchungTriageService;
    private final UntersuchungArztBeurteilungService untersuchungArztBeurteilungService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final UntersuchungLaeusekontrolleService untersuchungLaeusekontrolleService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final UntersuchungImpfungenMassnahmenService untersuchungImpfungenMassnahmenService;
    private final UntersuchungBewegungsapparatService untersuchungBewegungsapparatService;
    private final UntersuchungSpracheService untersuchungSpracheService;
    private final UntersuchungVorabklaerungenService untersuchungVorabklaerungenService;
    private final UntersuchungSchulrelevanteBeeintraechtigungService untersuchungSchulrelevanteBeeintraechtigungService;
    private final AnonymerEintragService anonymerEintragService;
    private final UntersuchungAbschlussService untersuchungAbschlussService;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;

    @NonNull
    public Untersuchung getOrCreateUntersuchung(
        @NonNull ID<Einladung> einladungId,
        boolean loadSubuntersuchungenEager
    ) {
        Einladung einladung = einladungService.getByIdLockedQuery(einladungId);
        einladung.disableOptimisticLockingCheck();
        Untersuchung untersuchung = einladung.getUntersuchung();
        if (einladung.getUntersuchung() == null) {
            untersuchung = Untersuchung.forEinladung(einladung);
            untersuchungRepo.persist(untersuchung);

            // Bei den Impfungen wird bei jeder Untersuchung die Werte des vorherigen Anlass als Grundlage genommen,
            // deshalb muss sichergestellt werden, dass die Impfungen vorhanden sind selbst wenn nie etwas im
            // Impfungen Tab gespeichert worden ist.
            createImpfungen(untersuchung);
        }
        if (loadSubuntersuchungenEager) {
            return untersuchungRepo.getByIdWithLoadedSubuntersuchungen(untersuchung.getId());
        }
        return einladung.getUntersuchung();

    }

    @NonNull
    public Untersuchung getOrCreateUntersuchungAnonym(
        @NonNull ID<AnonymerEintrag> anonymerEintragID,
        boolean loadSubuntersuchungenEager
    ) {
        AnonymerEintrag anonymerEintrag = anonymerEintragService.getByIdLockedQuery(anonymerEintragID);
        anonymerEintrag.disableOptimisticLockingCheck();
        Untersuchung untersuchung = anonymerEintrag.getUntersuchung();
        if (anonymerEintrag.getUntersuchung() == null) {
            untersuchung = Untersuchung.forAnonymerEintrag(anonymerEintrag);
            untersuchungRepo.persist(untersuchung);

            // Erstellen der Impfungen ohne History
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        }
        if (loadSubuntersuchungenEager) {
            return untersuchungRepo.getByIdWithLoadedSubuntersuchungen(untersuchung.getId());
        }
        return anonymerEintrag.getUntersuchung();

    }

    private void createImpfungen(@NonNull Untersuchung untersuchung) {
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        Optional<Einladung> lastEinladung =
            einladungService.findLastEinladungForKind(
                impfungen.getUntersuchung().getAnlass(),
                impfungen.getUntersuchung().getEinladungRequired().getDossier().getKind().getId());

        completeImpfungenBasedOnLastAnlass(impfungen, lastEinladung);
        createCompleteImpfstatusBasedOnLastAnlass(impfungen, lastEinladung);
        createCompleteImpfungenInternBasedOnLastAnlass(impfungen, lastEinladung);
        createImpfungenMassnahmen(impfungen, lastEinladung);
    }

    private void completeImpfungenBasedOnLastAnlass(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull Optional<Einladung> lastEinladung
    ) {
        if (lastEinladung.isPresent() && lastEinladung.get().getUntersuchung() != null && lastEinladung.get().getUntersuchung().getUntersuchungImpfungen() != null) {
            UntersuchungImpfungen lastImpfungen = lastEinladung.get().getUntersuchung().getUntersuchungImpfungen();
            impfungen.disableOptimisticLockingCheck();
            // Diese Checkboxen müssen übernommen werden, da es ansonsten beim Übernehmen des Impfstatus zu Fehlern im entsprechenden Validator kommen kann.
            impfungen.setImpfausweisUnvollstaendig(lastImpfungen.isImpfausweisUnvollstaendig());
            impfungen.setImpfausweisNichtVorhanden(lastImpfungen.isImpfausweisNichtVorhanden());
            impfungen.setImpfausweisZweiPlusEinsSchema(lastImpfungen.isImpfausweisZweiPlusEinsSchema());
        }
    }

    public void createCompleteImpfstatusBasedOnLastAnlass(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull Optional<Einladung> lastEinladung
    ) {
        List<UntersuchungImpfstatusKrankheit> impfstatusLastAnlass = Collections.emptyList();
        List<UntersuchungImpfungIntern> impfungenInternListLastAnlass = Collections.emptyList();
        if (lastEinladung.isPresent() && lastEinladung.get().getUntersuchung() != null) {
            impfstatusLastAnlass = untersuchungImpfstatusKrankheitService.findByUntersuchung(lastEinladung.get()
                .getUntersuchung()
                .getId());
            impfungenInternListLastAnlass =
                untersuchungImpfungInternService.findByUntersuchung(lastEinladung.get().getUntersuchung().getId());
        }

        List<UntersuchungImpfstatusKrankheit> impfstatusKrankheitList =
            UntersuchungImpfungenUtil.completeListFromExistingForEachKrankheit(
                impfungen,
                impfstatusLastAnlass,
                impfungenInternListLastAnlass);

        impfstatusKrankheitList.forEach(untersuchungImpfstatusKrankheitService::persist);
    }

    public void createCompleteImpfstatusWithoutHistory(@NonNull UntersuchungImpfungen impfungen) {
        List<UntersuchungImpfstatusKrankheit> impfstatusKrankheitList = UntersuchungImpfungenUtil.completeListFromExistingForEachKrankheit(impfungen);
        impfstatusKrankheitList.forEach(
            impfstatusKrankheit -> untersuchungImpfstatusKrankheitService.persist(impfstatusKrankheit, false)
        );
    }

    public void createCompleteImpfungenInternBasedOnLastAnlass(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull Optional<Einladung> lastEinladung
    ) {
        // Eigentlich sollten wir nur das erste Mal kopieren. Da das Objekt aber immer schon existiert
        // kopieren wir wenn die Liste (noch) leer ist. Das ist eigentlich zu viel. Insbesondere sollten
        // wir bei abgeschlossenen Anlaessen nicht mehr kopieren.
        if (lastEinladung.isPresent() && lastEinladung.get().getUntersuchung() != null) {
            List<UntersuchungImpfungIntern> oldImpfungen =
                untersuchungImpfungInternService.findByUntersuchung(lastEinladung.get()
                    .getUntersuchung()
                    .getId());

            oldImpfungen.forEach(impfung -> untersuchungImpfungInternService.persist(UntersuchungImpfungenUtil.copyImpfung(
                impfungen,
                impfung))
            );
        }
    }

    public void createImpfungenMassnahmen(
        @NonNull UntersuchungImpfungen impfungen,
        @NonNull Optional<Einladung> lastEinladung
    ) {
        Optional<String> achtungLastEinladung = getMassnahmeAchtungFromLastEinladung(lastEinladung);

        // UntersuchungImpfungenMassnahmen muss nur kopiert werden falls Achtung in der vorherigen Untersuchung
        // ausgefüllt worden ist.
        if (achtungLastEinladung.isPresent()) {
            UntersuchungImpfungenMassnahmen massnahmen = new UntersuchungImpfungenMassnahmen();
            massnahmen.setUntersuchungImpfungen(impfungen);
            impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);
            massnahmen.setAchtung(achtungLastEinladung.get());

            untersuchungImpfungenMassnahmenService.persist(massnahmen);
        }
    }

    private static Optional<String> getMassnahmeAchtungFromLastEinladung(Optional<Einladung> lastEinladung) {
        return lastEinladung
            .map(Einladung::getUntersuchung)
            .map(Untersuchung::getUntersuchungImpfungen)
            .map(UntersuchungImpfungen::getUntersuchungImpfungenMassnahmen)
            .map(UntersuchungImpfungenMassnahmen::getAchtung);
    }

    @NonNull
    public Untersuchung getOrCreateUntersuchung(@NonNull HasUntersuchungIDWrapper wrappedId) {
        if (wrappedId.isAnonymerEintrag()) {
            return getOrCreateUntersuchungAnonym(wrappedId.toAnonymerEintragId(), false);
        }

        return getOrCreateUntersuchung(wrappedId.toEinladungId(), false);
    }

    // the getOrCreateUntersuchung from above and stop using the one below
    @NonNull
    public Untersuchung getOrCreateUntersuchung(@NonNull ID<Einladung> einladungId) {
        return getOrCreateUntersuchung(einladungId, false);
    }

    public void removeUntersuchung(@NonNull Untersuchung untersuchung) {
        untersuchungImpfungenService.removeByUntersuchung(untersuchung);
        untersuchungRepo.remove(untersuchung.getId());
    }

    public void untersuchungAbschliessen(
        @NotNull @NonNull Untersuchung untersuchung
    ) {
        FeatureEnabledSetting nichtGeprueftGrundFeature =
            tenantSettingsService.getTenantSetting().getTenantSettingFeatures().getNichtGeprueftGrundFeature();
        DeploymentEnvironment deploymentEnvironment = configService.getEnvironment();
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv =
            nichtGeprueftGrundFeature.isEnabledForDeploymentEnvironment(deploymentEnvironment);

        untersuchungAbschliessen(untersuchung, nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv);
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean needsNichtGeprueftGrund
    ) {
        untersuchungDurchfuehrungService.untersuchungAbschliessen(untersuchung);
        untersuchungImpfungenService.untersuchungAbschliessen(untersuchung);
        untersuchungSehenBeurteilungService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungHoerenAudiogrammService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungHoerenOtoskopieService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungGroesseGewichtService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungTriageService.untersuchungAbschliessen(untersuchung);
        untersuchungLaeusekontrolleService.untersuchungAbschliessen(untersuchung);
        untersuchungArztBeurteilungService.untersuchungAbschliessen(untersuchung);
        untersuchungMassnahmenService.untersuchungAbschliessen(untersuchung);
        untersuchungSchulrelevanteBeeintraechtigungService.untersuchungAbschliessen(
            untersuchung,
            needsNichtGeprueftGrund);
        untersuchungSpracheService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungBewegungsapparatService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungVorabklaerungenService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungAbschlussService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
    }
}
