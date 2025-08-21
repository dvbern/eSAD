package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungHoerenOtoskopieService {

    private final UntersuchungHoerenOtoskopieRepo hoerenRepo;

    @NonNull
    public UntersuchungHoerenOtoskopie persist(@NonNull UntersuchungHoerenOtoskopie hoerenOtoskopie) {
        hoerenRepo.persist(hoerenOtoskopie);
        return hoerenOtoskopie;
    }

    @NonNull
    public UntersuchungHoerenOtoskopie getById(@NonNull ID<UntersuchungHoerenOtoskopie> hoerenID) {
        return hoerenRepo.getById(hoerenID);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie =
            UntersuchungHoerenOtoskopie.forUntersuchungAbschliessen(untersuchung);
        persist(hoerenOtoskopie);
    }
}
