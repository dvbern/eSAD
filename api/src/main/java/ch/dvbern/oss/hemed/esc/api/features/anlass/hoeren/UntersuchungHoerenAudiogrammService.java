package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungHoerenAudiogrammService {

    private final UntersuchungHoerenAudiogrammRepo hoerenRepo;

    @NonNull
    public UntersuchungHoerenAudiogramm persist(@NonNull UntersuchungHoerenAudiogramm hoeren) {
        hoerenRepo.persist(hoeren);
        return hoeren;
    }

    @NonNull
    public UntersuchungHoerenAudiogramm persistWithOptimisticLocking(@NonNull UntersuchungHoerenAudiogramm entity, @NonNull Long versionFromFrontend) {
        return hoerenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    public void cascadeOptimisticLocking(@NonNull Audiogramm audiogrammRechts, long version) {
        hoerenRepo.cascadeOptimisticLocking(audiogrammRechts, version);
    }

    @NonNull
    public UntersuchungHoerenAudiogramm getById(@NonNull ID<UntersuchungHoerenAudiogramm> hoerenID) {
        return hoerenRepo.getById(hoerenID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogramm.forUntersuchungAbschliessen(untersuchung);
        hoeren.disableOptimisticLockingCheck();
        persist(hoeren);
    }
}
