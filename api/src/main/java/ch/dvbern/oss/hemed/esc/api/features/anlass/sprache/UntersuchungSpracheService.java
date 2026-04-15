package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class UntersuchungSpracheService {

    private final UntersuchungSpracheRepo untersuchungSpracheRepo;
    private final UntersuchungRepo untersuchungRepo;

    @NonNull
    public UntersuchungSprache persist(@NonNull UntersuchungSprache triage) {
        untersuchungSpracheRepo.persist(triage);
        return triage;
    }

    @NonNull
    public UntersuchungSprache persistWithOptimisticLocking(
        @NonNull UntersuchungSprache entity,
        @NonNull Long versionFromFrontend) {
        return untersuchungSpracheRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungSprache getById(@NonNull ID<UntersuchungSprache> untersuchungSpracheID) {
        return untersuchungSpracheRepo.getById(untersuchungSpracheID);
    }

    @Nullable
    public UntersuchungSprache getByUntersuchung(@NonNull ID<Untersuchung> untersuchungID) {
        Untersuchung untersuchung = untersuchungRepo.getById(untersuchungID);
        if (untersuchung.getUntersuchungSprache() != null) {
            return untersuchung.getUntersuchungSprache();
        }
        return null;
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungSprache sprache = UntersuchungSprache.forUntersuchungAbschliessen(
            untersuchung,
            nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
        );
        sprache.disableOptimisticLockingCheck();
        persist(sprache);
    }
}
