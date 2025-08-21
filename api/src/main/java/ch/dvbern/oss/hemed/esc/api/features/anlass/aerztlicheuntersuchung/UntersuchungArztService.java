package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungArztService {

    private final UntersuchungArztRepo arztRepo;

    @NonNull
    public UntersuchungArzt persist(@NonNull UntersuchungArzt arzt) {
        arztRepo.persist(arzt);
        return arzt;
    }

    @NonNull
    public UntersuchungArzt getById(@NonNull ID<UntersuchungArzt> arztID) {
        return arztRepo.getById(arztID);
    }

    public void untersuchungAbschliessen(Untersuchung untersuchung) {
        UntersuchungArzt arzt = UntersuchungArzt.forUntersuchungAbschliessen(untersuchung);
        persist(arzt);
    }
}
