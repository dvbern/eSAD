package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class ImpfstoffChargeService {

    private final ImpfstoffChargeRepo impfstoffChargeRepo;

    @NonNull
    public ImpfstoffCharge persist(@NonNull ImpfstoffCharge charge) {
        impfstoffChargeRepo.persist(charge);
        return charge;
    }

    @NonNull
    public List<ImpfstoffCharge> findAll() {
        return impfstoffChargeRepo.findAll();
    }

    @NonNull
    public Optional<ImpfstoffCharge> findById(@NonNull ID<ImpfstoffCharge> chargeID) {
        return impfstoffChargeRepo.findById(chargeID);
    }

    @NonNull
    public ImpfstoffCharge getById(@NonNull ID<ImpfstoffCharge> chargeID) {
        return impfstoffChargeRepo.getById(chargeID);
    }

    @NonNull
    public List<ImpfstoffCharge> findAllByImpfstoff(@NonNull ID<Impfstoff> impfstoffId) {
        return impfstoffChargeRepo.findAllByImpfstoff(impfstoffId);
    }

    public void remove(@NonNull ID<ImpfstoffCharge> impfstoffChargeId) {
        impfstoffChargeRepo.remove(impfstoffChargeId);
    }
}
