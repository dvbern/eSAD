package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungImpfungenDataMinimalService {

    private final UntersuchungImpfungenDataMinimalRepo impfungenDataMinimalRepo;

    @NonNull
    public UntersuchungImpfungenDataMinimal persist(@NonNull UntersuchungImpfungenDataMinimal dataMinimal) {
        impfungenDataMinimalRepo.persist(dataMinimal);
        return dataMinimal;
    }

    @NonNull
    public UntersuchungImpfungenDataMinimal persistWithOptimisticLocking(@NonNull UntersuchungImpfungenDataMinimal dataMinimal, @NonNull Long versionFromFrontend) {
        return impfungenDataMinimalRepo.persistWithOptimisticLocking(dataMinimal, versionFromFrontend);
    }

    @NonNull
    public UntersuchungImpfungenDataMinimal getById(@NonNull ID<UntersuchungImpfungenDataMinimal> dataMinimalID) {
        return impfungenDataMinimalRepo.getById(dataMinimalID);
    }

    public void untersuchungAbschliessen(@NonNull UntersuchungImpfungen impfungen) {
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimal.forUntersuchungAbschliessen(impfungen);
        dataMinimal.disableOptimisticLockingCheck();
        persist(dataMinimal);
    }
}
