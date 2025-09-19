package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungArztBemerkungService {

    private final UntersuchungArztBemerkungRepo arztBemerkungRepo;

    @NonNull
    public UntersuchungArztBemerkung persist(@NonNull UntersuchungArztBemerkung arzt) {
        arztBemerkungRepo.persist(arzt);
        return arzt;
    }

    @NonNull
    public UntersuchungArztBemerkung persistWithOptimisticLocking(@NonNull UntersuchungArztBemerkung bemerkung, @NonNull Long versionFromFrontend) {
        return arztBemerkungRepo.persistWithOptimisticLocking(bemerkung, versionFromFrontend);
    }

    @NonNull
    public UntersuchungArztBemerkung getById(@NonNull ID<UntersuchungArztBemerkung> arztID) {
        return arztBemerkungRepo.getById(arztID);
    }
}
