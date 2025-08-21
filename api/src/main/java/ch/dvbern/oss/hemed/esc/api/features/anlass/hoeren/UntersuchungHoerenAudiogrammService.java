package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
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
    public UntersuchungHoerenAudiogramm getById(@NonNull ID<UntersuchungHoerenAudiogramm> hoerenID) {
        return hoerenRepo.getById(hoerenID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogramm.forUntersuchungAbschliessen(untersuchung);
        persist(hoeren);
    }
}
