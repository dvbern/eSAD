package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
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
        }
        if (loadSubuntersuchungenEager) {
            return untersuchungRepo.getByIdWithLoadedSubuntersuchungen(untersuchung.getId());
        }
        return einladung.getUntersuchung();

    }

    @NonNull
    public Untersuchung getOrCreateUntersuchung(@NonNull ID<Einladung> einladungId) {
        return getOrCreateUntersuchung(einladungId, false);
    }

    public void removeUntersuchung(@NonNull Untersuchung untersuchung) {
        untersuchungImpfungenService.removeByUntersuchung(untersuchung);
        untersuchungRepo.remove(untersuchung.getId());
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        untersuchungDurchfuehrungService.untersuchungAbschliessen(untersuchung);
        untersuchungImpfungenService.untersuchungAbschliessen(untersuchung);
        untersuchungSehenBeurteilungService.untersuchungAbschliessen(untersuchung);
        untersuchungHoerenAudiogrammService.untersuchungAbschliessen(untersuchung);
        untersuchungHoerenOtoskopieService.untersuchungAbschliessen(untersuchung);
        untersuchungGroesseGewichtService.untersuchungAbschliessen(untersuchung);
        untersuchungTriageService.untersuchungAbschliessen(untersuchung);
        untersuchungArztBeurteilungService.untersuchungAbschliessen(untersuchung);
        untersuchungMassnahmenService.untersuchungAbschliessen(untersuchung);
    }
}
