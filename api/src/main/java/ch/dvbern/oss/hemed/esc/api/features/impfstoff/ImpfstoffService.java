package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class ImpfstoffService {

    private final ImpfstoffRepo impfstoffRepo;

    @NonNull
    public Impfstoff persist(@NonNull Impfstoff impfstoff) {
        impfstoffRepo.persist(impfstoff);
        return impfstoff;
    }

    @NonNull
    public Impfstoff persistWithOptimisticLocking(@NonNull Impfstoff impfstoff, @NonNull Long versionFromFrontend) {
        impfstoffRepo.persistWithOptimisticLocking(impfstoff, versionFromFrontend);
        return impfstoff;
    }

    @NonNull
    public List<Impfstoff> findAll() {
        return impfstoffRepo.findAll();
    }

    @NonNull
    public Optional<Impfstoff> findById(@NonNull ID<Impfstoff> chargeID) {
        return impfstoffRepo.findById(chargeID);
    }

    @NonNull
    public Impfstoff getById(@NonNull ID<Impfstoff> impfstoffID) {
        return impfstoffRepo.getById(impfstoffID);
    }

    public void remove(@NonNull ID<Impfstoff> impfstoffID) {
        impfstoffRepo.remove(impfstoffID);
    }
}
