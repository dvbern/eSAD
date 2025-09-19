package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungSehenBeurteilungService {

    private final UntersuchungSehenBeurteilungRepo sehenRepo;

    @NonNull
    public UntersuchungSehenBeurteilung persist(@NonNull UntersuchungSehenBeurteilung sehen) {
        sehenRepo.persist(sehen);
        return sehen;
    }

    @NonNull
    public UntersuchungSehenBeurteilung persistWithOptimisticLocking(@NonNull UntersuchungSehenBeurteilung entity, @NonNull Long versionFromFrontend) {
        return sehenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungSehenBeurteilung getById(@NonNull ID<UntersuchungSehenBeurteilung> sehenID) {
        return sehenRepo.getById(sehenID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung sehen = UntersuchungSehenBeurteilung.forUntersuchungAbschliessen(untersuchung);
        sehen.disableOptimisticLockingCheck();
        persist(sehen);
    }
}
