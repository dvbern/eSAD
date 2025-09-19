package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class ArztService {
    private final ArztRepo arztRepo;

    @NonNull
    public Arzt persist(@NonNull Arzt arzt) {
        arztRepo.persist(arzt);
        return arzt;
    }

    @NonNull
    public Arzt persistWithOptimisticLocking(@NonNull Arzt arzt, @NonNull Long versionFromFrontend) {
        arztRepo.persistWithOptimisticLocking(arzt, versionFromFrontend);
        return arzt;
    }

    public void remove(@NonNull ID<Arzt> arztID) {
        arztRepo.remove(arztID);
    }

    @NonNull
    public List<Arzt> findAll() {
        return arztRepo.findAll();
    }

    @NonNull
    public Optional<Arzt> findById(@NonNull ID<Arzt> arztID) {
        return arztRepo.findById(arztID);
    }

    @NonNull
    public Arzt getById(@NonNull ID<Arzt> arztID) {
        return arztRepo.getById(arztID);
    }

    @NonNull
    public List<Arzt> findByIds(@NonNull List<ID<Arzt>> arztIDs) {
        return arztRepo.findByIds(arztIDs);
    }
}
