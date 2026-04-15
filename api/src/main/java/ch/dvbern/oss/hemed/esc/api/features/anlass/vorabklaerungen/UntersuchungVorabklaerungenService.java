package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungVorabklaerungenService {

    private final UntersuchungVorabklaerungenRepo vorabklaerungenRepo;

    @NonNull
    public UntersuchungVorabklaerungen persist(@NonNull UntersuchungVorabklaerungen vorabklaerungen) {
        vorabklaerungenRepo.persist(vorabklaerungen);
        return vorabklaerungen;
    }

    @NonNull
    public UntersuchungVorabklaerungen persistWithOptimisticLocking(
        @NonNull UntersuchungVorabklaerungen entity,
        @NonNull Long versionFromFrontend
    ) {
        return vorabklaerungenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungVorabklaerungen getById(@NonNull ID<UntersuchungVorabklaerungen> vorabklaerungenID) {
        return vorabklaerungenRepo.getById(vorabklaerungenID);
    }

    public void untersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungVorabklaerungen vorabklaerungen = UntersuchungVorabklaerungen.forUntersuchungAbschliessen(
            untersuchung,
            nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
        );
        vorabklaerungen.disableOptimisticLockingCheck();
        persist(vorabklaerungen);
    }
}
