package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class UntersuchungSehenBemerkungFragebogenService {

    private final UntersuchungSehenBemerkungFragebogenRepo sehenRepo;

    @NonNull
    public UntersuchungSehenBemerkungFragebogen persist(@NonNull UntersuchungSehenBemerkungFragebogen sehen) {
        sehenRepo.persist(sehen);
        return sehen;
    }

    @NonNull
    public UntersuchungSehenBemerkungFragebogen persistWithOptimisticLocking(@NonNull UntersuchungSehenBemerkungFragebogen entity, @NonNull Long versionFromFrontend) {
        return sehenRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public UntersuchungSehenBemerkungFragebogen getById(@NonNull ID<UntersuchungSehenBemerkungFragebogen> sehenID) {
        return sehenRepo.getById(sehenID);
    }
}
