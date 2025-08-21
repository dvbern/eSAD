package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungGroesseGewichtService {

    private final UntersuchungGroesseGewichtRepo groesseGewichtRepo;

    @NonNull
    public UntersuchungGroesseGewicht persist(@NonNull UntersuchungGroesseGewicht groesseGewicht) {
        groesseGewichtRepo.persist(groesseGewicht);
        return groesseGewicht;
    }

    @NonNull
    public UntersuchungGroesseGewicht getById(@NonNull ID<UntersuchungGroesseGewicht> groesseGewichtID) {
        return groesseGewichtRepo.getById(groesseGewichtID);
    }

    @NonNull
    public List<UntersuchungGroesseGewicht> findPreviousByKind(
        @NonNull ID<Kind> kindID,
        @NonNull LocalDate stichtag
    ) {
        return groesseGewichtRepo.findPreviousByKind(kindID, stichtag);
    }

    public void untersuchungAbschliessen(@NonNull Untersuchung untersuchung) {
        UntersuchungGroesseGewicht groesseGewicht = UntersuchungGroesseGewicht.forUntersuchungAbschliessen(untersuchung);
        persist(groesseGewicht);
    }
}
