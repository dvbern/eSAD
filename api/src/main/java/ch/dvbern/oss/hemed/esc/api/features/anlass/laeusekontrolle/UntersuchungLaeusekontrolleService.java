package ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungLaeusekontrolleService {

    private final UntersuchungLaeusekontrolleRepo laeusekontrolleRepo;

    @NonNull
    public UntersuchungLaeusekontrolle persist(@NonNull UntersuchungLaeusekontrolle laeusekontrolle) {
        laeusekontrolleRepo.persist(laeusekontrolle);
        return laeusekontrolle;
    }

    @NonNull
    public UntersuchungLaeusekontrolle persistWithOptimisticLocking(@NonNull UntersuchungLaeusekontrolle entity, @NonNull Long versionFromFrontend) {
        return laeusekontrolleRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungLaeusekontrolle getById(@NonNull ID<UntersuchungLaeusekontrolle> laeusekontrolleID) {
        return laeusekontrolleRepo.getById(laeusekontrolleID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungLaeusekontrolle laeusekontrolle = UntersuchungLaeusekontrolle.forUntersuchungAbschliessen(untersuchung);
        laeusekontrolle.disableOptimisticLockingCheck();
        persist(laeusekontrolle);
    }
}
