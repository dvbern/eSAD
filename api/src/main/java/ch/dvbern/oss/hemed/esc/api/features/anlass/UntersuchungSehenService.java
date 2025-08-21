package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungSehenService {

    private final UntersuchungSehenRepo sehenRepo;

    @NonNull
    public UntersuchungSehen persist(@NonNull UntersuchungSehen sehen) {
        sehenRepo.persist(sehen);
        return sehen;
    }

    @NonNull
    public UntersuchungSehen getById(@NonNull ID<UntersuchungSehen> sehenID) {
        return sehenRepo.getById(sehenID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungSehen sehen = UntersuchungSehen.forUntersuchungAbschliessen(untersuchung);
        persist(sehen);
    }
}
