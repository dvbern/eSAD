package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungKindDatenService {

    private final UntersuchungKindDatenRepo untersuchungKindDatenRepo;


    @NonNull
    public UntersuchungKindDaten persist(@NonNull UntersuchungKindDaten kindDaten) {
        untersuchungKindDatenRepo.persist(kindDaten);
        return kindDaten;
    }

    public void removeByUntersuchung(@NonNull Untersuchung untersuchung) {
        if (untersuchung.getUntersuchungKindDaten() != null) {
            untersuchungKindDatenRepo.remove(untersuchung.getUntersuchungKindDaten().getId());
        }
    }
}
