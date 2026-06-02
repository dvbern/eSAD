package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class AnonymerEintragService {

    private final AnonymerEintragRepo anonymerEintragRepo;
    private final UntersuchungService untersuchungService;
    private final AnlassService anlassService;

    @NonNull
    public AnonymerEintrag getById(@NonNull ID<AnonymerEintrag> anonymerEintragID) {
        return anonymerEintragRepo.getById(anonymerEintragID);
    }

    @NonNull
    public AnonymerEintrag persist(@NonNull AnonymerEintrag anonymerEintrag) {
        anonymerEintragRepo.persist(anonymerEintrag);
        return anonymerEintrag;
    }

    public AnonymerEintrag getByIdLockedQuery(@NonNull ID<AnonymerEintrag> anonymerEintragID) {
        return anonymerEintragRepo.getByIdLockedQuery(anonymerEintragID);
    }

    public void anonymerEintragAbschliessen(@NotNull @NonNull ID<AnonymerEintrag> anonymerEintragID) {
        // Untersuchungen abschliessen
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintragID, false);
        untersuchungService.untersuchungAbschliessen(untersuchung);

        // Anlass auf ABGESCHLOSSEN setzen
        AnonymerEintrag anonymerEintrag = getByIdLockedQuery(anonymerEintragID);
        Anlass anlass = anonymerEintrag.getAnlass();
        anlass.disableOptimisticLockingCheck();
        anlass.setAnlassStatus(AnlassStatus.ABGESCHLOSSEN);
        anlassService.persist(anlass);
    }
}
