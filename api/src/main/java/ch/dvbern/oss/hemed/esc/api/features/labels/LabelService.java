package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepo labelRepo;


    @NonNull
    public Label persist(@NonNull Label label) {
        labelRepo.persist(label);
        return label;
    }

    @NonNull
    public Label persistWithOptimisticLocking(@NonNull Label entity, @NonNull Long versionFromFrontend) {
        return labelRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    public void remove(@NonNull ID<Label> labelID) {
        labelRepo.remove(labelID);
    }

    @NonNull
    public List<Label> findAll() {
        return labelRepo.findAll();
    }

    @NonNull
    public Optional<Label> findById(@NonNull ID<Label> labelID) {
        return labelRepo.findById(labelID);
    }

    @NonNull
    public Label getById(@NonNull ID<Label> labelID) {
        return labelRepo.getById(labelID);
    }

    @NonNull
    public List<Label> filterByType(@NonNull LabelTyp type) {
        return labelRepo.filterByType(type);
    }
}
