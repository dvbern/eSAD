package ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungService;
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
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungImpfungenUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    private void createImpfungen(@NonNull Untersuchung untersuchung) {
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());

        createCompleteImpfstatusBasedOnLastAnlass(impfungen);

        createCompleteImpfungenInternBasedOnLastAnlass(impfungen);

        createImpfungenMassnahmen(impfungen);
    }

    public void createCompleteImpfstatusBasedOnLastAnlass(@NonNull UntersuchungImpfungen impfungen) {
        Optional<Einladung> lastEinladung =
            einladungService.findLastEinladungForKind(
                impfungen.getUntersuchung().getEinladung().getAnlass(),
                impfungen.getUntersuchung().getEinladung().getDossier().getKind().getId());

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

    public void createCompleteImpfungenInternBasedOnLastAnlass(@NonNull UntersuchungImpfungen impfungen) {
        // Eigentlich sollten wir nur das erste Mal kopieren. Da das Objekt aber immer schon existiert
        // kopieren wir wenn die Liste (noch) leer ist. Das ist eigentlich zu viel. Insbesondere sollten
        // wir bei abgeschlossenen Anlaessen nicht mehr kopieren.
        Optional<Einladung> lastEinladung = einladungService.findLastEinladungForKind(
            impfungen.getUntersuchung().getEinladung().getAnlass(),
            impfungen.getUntersuchung().getEinladung().getDossier().getKind().getId());

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

    public void createImpfungenMassnahmen(@NonNull UntersuchungImpfungen impfungen) {
        Optional<Einladung> lastEinladung = einladungService.findLastEinladungForKind(
            impfungen.getUntersuchung().getEinladung().getAnlass(),
            impfungen.getUntersuchung().getEinladung().getDossier().getKind().getId());

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
    public Untersuchung getOrCreateUntersuchung(@NonNull ID<Einladung> einladungId) {
        return getOrCreateUntersuchung(einladungId, false);
    }

    public void removeUntersuchung(@NonNull Untersuchung untersuchung) {
        untersuchungImpfungenService.removeByUntersuchung(untersuchung);
        untersuchungRepo.remove(untersuchung.getId());
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
        untersuchungSchulrelevanteBeeintraechtigungService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungSpracheService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungBewegungsapparatService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
        untersuchungVorabklaerungenService.untersuchungAbschliessen(untersuchung, needsNichtGeprueftGrund);
    }
}
