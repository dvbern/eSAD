package ch.dvbern.oss.hemed.esc.api.features.anlass.triage;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class UntersuchungTriageService {

    private final UntersuchungTriageRepo triageRepo;
    private final UntersuchungRepo untersuchungRepo;

    @NonNull
    public UntersuchungTriage persist(@NonNull UntersuchungTriage triage) {
        triageRepo.persist(triage);
        return triage;
    }

    @NonNull
    public UntersuchungTriage persistWithOptimisticLocking(
        @NonNull UntersuchungTriage entity,
        @NonNull Long versionFromFrontend
    ) {
        return triageRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungTriage getById(@NonNull ID<UntersuchungTriage> triageID) {
        return triageRepo.getById(triageID);
    }

    @Nullable
    public UntersuchungTriage getUntersuchungTriage(@NonNull ID<Untersuchung> untersuchungID) {
        Untersuchung untersuchung = untersuchungRepo.getById(untersuchungID);
        if (untersuchung.getUntersuchungTriage() != null) {
            return untersuchung.getUntersuchungTriage();
        }
        return null;
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungTriage triage = UntersuchungTriage.forUntersuchungAbschliessen(untersuchung);
        triage.disableOptimisticLockingCheck();
        persist(triage);
    }
}
