package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class InfomaterialService {

    private final InfomaterialRepo infomaterialRepo;

    @NonNull
    public Infomaterial persist(@NonNull Infomaterial infomaterial) {
        infomaterialRepo.persist(infomaterial);
        return infomaterial;
    }

    @NonNull
    public Infomaterial persistWithOptimisticLocking(@NonNull Infomaterial infomaterial, @NonNull Long versionFromFrontend) {
        infomaterialRepo.persistWithOptimisticLocking(infomaterial, versionFromFrontend);
        return infomaterial;
    }

    public void remove(@NonNull ID<Infomaterial> infomaterialID) {
        infomaterialRepo.remove(infomaterialID);
    }

    @NonNull
    public List<Infomaterial> findAll() {
        return infomaterialRepo.findAll();
    }

    @NonNull
    public List<Infomaterial> findAllActive() {
        return infomaterialRepo.findAllActive();
    }

    @NonNull
    public Optional<Infomaterial> findById(@NonNull ID<Infomaterial> infomaterialID) {
        return infomaterialRepo.findById(infomaterialID);
    }

    @NonNull
    public Infomaterial getById(@NonNull ID<Infomaterial> id) {
        return infomaterialRepo.getById(id);
    }

}
