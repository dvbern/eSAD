package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungRepo;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
public class UntersuchungSchulrelevanteBeeintraechtigungService {

    private final UntersuchungSchulrelevanteBeeintraechtigungRepo beeintraechtigungRepo;
    private final UntersuchungRepo untersuchungRepo;

    @NonNull
    public UntersuchungSchulrelevanteBeeintraechtigung persist(
        @NonNull UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung) {
        beeintraechtigungRepo.persist(beeintraechtigung);
        return beeintraechtigung;
    }

    @NonNull
    public UntersuchungSchulrelevanteBeeintraechtigung persistWithOptimisticLocking(
        @NonNull UntersuchungSchulrelevanteBeeintraechtigung entity,
        @NonNull Long versionFromFrontend) {
        return beeintraechtigungRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungSchulrelevanteBeeintraechtigung getById(
        @NonNull ID<UntersuchungSchulrelevanteBeeintraechtigung> untersuchungSpracheID) {
        return beeintraechtigungRepo.getById(untersuchungSpracheID);
    }

    @Nullable
    public UntersuchungSchulrelevanteBeeintraechtigung getByUntersuchung(@NonNull ID<Untersuchung> untersuchungID) {
        Untersuchung untersuchung = untersuchungRepo.getById(untersuchungID);
        if (untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung() != null) {
            return untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung();
        }
        return null;
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungSchulrelevanteBeeintraechtigung untersuchungSchulrelevanteBeeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigung.forUntersuchungAbschliessen(
                untersuchung,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            );
        untersuchungSchulrelevanteBeeintraechtigung.disableOptimisticLockingCheck();
        persist(untersuchungSchulrelevanteBeeintraechtigung);
    }
}
