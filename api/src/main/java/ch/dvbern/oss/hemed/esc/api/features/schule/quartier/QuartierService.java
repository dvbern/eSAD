package ch.dvbern.oss.hemed.esc.api.features.schule.quartier;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class QuartierService {

    private final QuartierRepo quartierRepo;

    @NonNull
    public Quartier getById(@NonNull ID<Quartier> id) {
        return quartierRepo.getById(id);
    }

    @NonNull
    public Optional<Quartier> findById(@NonNull ID<Quartier> id) {
        return quartierRepo.findById(id);
    }

    @NonNull
    public List<Quartier> findAll() {
        return quartierRepo.findAll();
    }

    @NonNull
    public Quartier persist(@NonNull Quartier quartier) {
        quartierRepo.persist(quartier);
        return quartier;
    }

    public void remove(@NonNull ID<Quartier> id) {
        quartierRepo.remove(id);
    }
}
