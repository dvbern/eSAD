package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungAbschlussService {

    private final UntersuchungAbschlussRepo abschlussRepo;

    @NonNull
    public UntersuchungAbschluss persist(@NonNull UntersuchungAbschluss abschluss) {
        abschlussRepo.persist(abschluss);
        return abschluss;
    }

    @NonNull
    public UntersuchungAbschluss persistWithOptimisticLocking(
        @NonNull UntersuchungAbschluss entity,
        @NonNull Long versionFromFrontend) {
        return abschlussRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungAbschluss getById(@NonNull ID<UntersuchungAbschluss> abschlussId) {
        return abschlussRepo.getById(abschlussId);
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungAbschluss abschluss = UntersuchungAbschluss.forUntersuchungAbschliessen(
            untersuchung,
            nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
        );
        abschluss.disableOptimisticLockingCheck();
        persist(abschluss);
    }

}
