package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungBewegungsapparatService {

    private final UntersuchungBewegungsapparatRepo bewegungsapparatRepo;

    @NonNull
    public UntersuchungBewegungsapparat persist(@NonNull UntersuchungBewegungsapparat bewegungsapparat) {
        bewegungsapparatRepo.persist(bewegungsapparat);
        return bewegungsapparat;
    }

    @NonNull
    public UntersuchungBewegungsapparat persistWithOptimisticLocking(
        @NonNull UntersuchungBewegungsapparat bewegungsapparat, @NonNull Long versionFromFrontend) {
        return bewegungsapparatRepo.persistWithOptimisticLocking(bewegungsapparat, versionFromFrontend);
    }

    @NonNull
    public UntersuchungBewegungsapparat getById(@NonNull ID<UntersuchungBewegungsapparat> bewegungsapparatId) {
        return bewegungsapparatRepo.getById(bewegungsapparatId);
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungBewegungsapparat bewegungsapparat = UntersuchungBewegungsapparat.forUntersuchungAbschliessen(
            untersuchung,
            nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
        );
        bewegungsapparat.disableOptimisticLockingCheck();
        persist(bewegungsapparat);
    }

}
