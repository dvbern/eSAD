package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepo dossierRepo;
    private final KindService kindService;

    @NonNull
    public Dossier persist(@NonNull Dossier dossier) {
        dossierRepo.persist(dossier);
        return dossier;
    }

    @NonNull
    public Dossier persistWithOptimisticLocking(@NonNull Dossier dossier, @NonNull Long versionFromFrontend) {
        dossierRepo.persistWithOptimisticLocking(dossier, versionFromFrontend);
        return dossier;
    }

    public void remove(@NonNull ID<Dossier> dossierID) {
        dossierRepo.remove(dossierID);
    }

    @NonNull
    public Dossier getById(@NonNull ID<Dossier> dossierID) {
        return dossierRepo.getById(dossierID);
    }

    @NonNull
    public Dossier getOrCreateByKind(@NonNull ID<Kind> kindID) {
        Optional<Dossier> dossierOptional = dossierRepo.findByKind(kindID);
        return dossierOptional.orElseGet(() -> {
            Kind kind = kindService.getById(kindID);
            return persist(Dossier.forKind(kind));
        });
    }

    @NonNull
    public Optional<Dossier> findByKind(@NonNull ID<Kind> kindID) {
        return dossierRepo.findByKind(kindID);
    }

    @NonNull
    public ID<Dossier> getDossierIdByKind(@NonNull ID<Kind> kindID) {
        Optional<Dossier> dossierOptional = dossierRepo.findByKind(kindID);
        return dossierOptional.orElseThrow().getId();
    }

    @NonNull
    public List<Dossier> findBySchule(@NonNull ID<Schule> schuleId) {
        return dossierRepo.findBySchule(schuleId);
    }
}
