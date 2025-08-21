package ch.dvbern.oss.hemed.esc.api.features.kinddeactivation;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindRepo;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class KindDeactivationService {
    private final KindDeactivationRepo kindDeactivationRepo;
    private final KindRepo kindRepo;

    @NonNull
    public List<ID<Kind>> findKinderToDeactivate(int monthsUnused, int manualKinderDeactivationAge, int batchsize) {
        return kindDeactivationRepo.findKinderToDeactivate(monthsUnused, manualKinderDeactivationAge, batchsize);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void setKindAktivStatusForKind(
        @NonNull ID<Kind> kindID,
        @NonNull EntityAktivStatus kindAktivStatus
    ) {
        Kind kind = kindRepo.getById(kindID);
        kind.setKindAktivStatus(kindAktivStatus);
        kindRepo.persist(kind);
    }
}
