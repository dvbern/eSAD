package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungArztBeurteilungService {

    private final UntersuchungArztBeurteilungRepo arztRepo;

    @NonNull
    public UntersuchungArztBeurteilung persist(@NonNull UntersuchungArztBeurteilung arzt) {
        arztRepo.persist(arzt);
        return arzt;
    }

    @NonNull
    public UntersuchungArztBeurteilung persistWithOptimisticLocking(@NonNull UntersuchungArztBeurteilung durchfuehrung, @NonNull Long versionFromFrontend) {
        return arztRepo.persistWithOptimisticLocking(durchfuehrung, versionFromFrontend);
    }

    @NonNull
    public UntersuchungArztBeurteilung getById(@NonNull ID<UntersuchungArztBeurteilung> arztID) {
        return arztRepo.getById(arztID);
    }

    public void untersuchungAbschliessen(Untersuchung untersuchung) {
        UntersuchungArztBeurteilung arzt = UntersuchungArztBeurteilung.forUntersuchungAbschliessen(untersuchung);
        arzt.disableOptimisticLockingCheck();
        persist(arzt);
    }
}
