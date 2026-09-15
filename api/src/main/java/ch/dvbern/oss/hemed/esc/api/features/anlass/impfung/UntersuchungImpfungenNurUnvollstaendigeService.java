package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungImpfungenNurUnvollstaendigeService {

    private final UntersuchungImpfungenNurUnvollstaendigeRepo impfungenNurUnvollstaendigeRepo;

    @NonNull
    public UntersuchungImpfungenNurUnvollstaendige persist(@NonNull UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige) {
        impfungenNurUnvollstaendigeRepo.persist(nurUnvollstaendige);
        return nurUnvollstaendige;
    }

    @NonNull
    public UntersuchungImpfungenNurUnvollstaendige persistWithOptimisticLocking(@NonNull UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige, @NonNull Long versionFromFrontend) {
        return impfungenNurUnvollstaendigeRepo.persistWithOptimisticLocking(nurUnvollstaendige, versionFromFrontend);
    }

    @NonNull
    public UntersuchungImpfungenNurUnvollstaendige getById(@NonNull ID<UntersuchungImpfungenNurUnvollstaendige> nurUnvollstaendigeID) {
        return impfungenNurUnvollstaendigeRepo.getById(nurUnvollstaendigeID);
    }

    public void untersuchungAbschliessen(@NonNull UntersuchungImpfungen impfungen) {
        UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige = UntersuchungImpfungenNurUnvollstaendige.forUntersuchungAbschliessen(impfungen);
        nurUnvollstaendige.disableOptimisticLockingCheck();
        persist(nurUnvollstaendige);
    }
}
