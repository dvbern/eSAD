package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class ImpfstoffChargeRepo extends BaseRepo<ImpfstoffCharge> {

    @NonNull
    public List<ImpfstoffCharge> findAll() {
        return db.findAll(QImpfstoffCharge.impfstoffCharge);
    }

    @NonNull
    public List<ImpfstoffCharge> findAllByImpfstoff(@NonNull ID<Impfstoff> impfstoffId) {
        return db.select(QImpfstoffCharge.impfstoffCharge)
            .from(QImpfstoffCharge.impfstoffCharge)
            .where(QImpfstoffCharge.impfstoffCharge.impfstoff.id.eq(impfstoffId.getUUID()))
            .fetch();
    }

    @NonNull
    public Optional<ImpfstoffCharge> findByImpfstoffAndName(@NonNull ID<Impfstoff> impfstoffId, @NonNull String chargeName) {
        return db.select(QImpfstoffCharge.impfstoffCharge)
            .from(QImpfstoffCharge.impfstoffCharge)
            .where(QImpfstoffCharge.impfstoffCharge.impfstoff.id.eq(impfstoffId.getUUID())
                .and(QImpfstoffCharge.impfstoffCharge.name.eq(chargeName)))
            .fetchOne();
    }
}
