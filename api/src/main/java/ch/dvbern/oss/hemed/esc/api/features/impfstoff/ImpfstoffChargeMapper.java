package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.PathParamConsistancyUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class ImpfstoffChargeMapper {

    private final ImpfstoffChargeService impfstoffChargeService;
    private final ImpfstoffService impfstoffService;

    @NonNull
    public static ImpfstoffChargeJson toJson(@NonNull ImpfstoffCharge charge) {
        return new ImpfstoffChargeJson(
            charge.getId(),
            charge.getImpfstoff().getId(),
            charge.getImpfstoff().getName(),
            new ImpfstoffChargeSaveJson(
                charge.getName(),
                charge.isAktiv(),
                charge.getVersion()
            )
        );
    }

    @NonNull
    private ImpfstoffCharge toEntity(@NonNull ImpfstoffCharge existing, @NonNull ImpfstoffChargeSaveJson saveJson) {
        existing.setName(saveJson.getName());
        existing.setAktiv(saveJson.isAktiv());
        return existing;
    }

    @NonNull
    public List<ImpfstoffChargeJson> findAll() {
        return impfstoffChargeService.findAll().stream()
            .map(ImpfstoffChargeMapper::toJson)
            .toList();
    }

    @NonNull
    private ImpfstoffCharge toEntityForCreate(
        @NonNull Impfstoff impfstoff,
        @NonNull ImpfstoffChargeSaveJson saveJson
    ) {
        return new ImpfstoffCharge(impfstoff, saveJson.getName(), saveJson.isAktiv());
    }

    @NonNull
    public List<ImpfstoffChargeJson> findAllByImpfstoff(@NonNull ID<Impfstoff> impfstoffId) {
        return impfstoffChargeService.findAllByImpfstoff(impfstoffId)
            .stream()
            .map(ImpfstoffChargeMapper::toJson)
            .toList();
    }

    public void deactivateAllChargenByImpfstoff(@NonNull ID<Impfstoff> impfstoffId) {
        impfstoffChargeService.findAllByImpfstoff(impfstoffId)
            .forEach(charge -> {
                charge.setAktiv(false);
                charge.disableOptimisticLockingCheck();
                impfstoffChargeService.persist(charge);
            });
    }

    @NonNull
    public ImpfstoffChargeJson create(
        @NonNull ID<Impfstoff> impfstoffId,
        @NonNull ImpfstoffChargeSaveJson impfstoffChargeSaveJson
    ) {
        Impfstoff impfstoff = impfstoffService.getById(impfstoffId);
        ImpfstoffCharge entity = toEntityForCreate(impfstoff, impfstoffChargeSaveJson);
        return toJson(impfstoffChargeService.persist(entity));
    }

    public void remove(@NonNull ID<ImpfstoffCharge> impfstoffChargeId) {
        impfstoffChargeService.remove(impfstoffChargeId);
    }

    @NonNull
    public ImpfstoffChargeJson update(
        @NonNull ID<ImpfstoffCharge> impfstoffChargeId,
        @NonNull ImpfstoffChargeSaveJson impfstoffChargeSaveJson
    ) {
        ImpfstoffCharge existing = impfstoffChargeService.getById(impfstoffChargeId);
        ImpfstoffCharge entity = toEntity(existing, impfstoffChargeSaveJson);
        return toJson(impfstoffChargeService.persistWithOptimisticLocking(entity, impfstoffChargeSaveJson.getVersion()));
    }

    public void requireConsistentPathParams(@NonNull ID<ImpfstoffCharge> impfstoffChargeId, @NonNull ID<Impfstoff> expectedImpfstoffId) {
        PathParamConsistancyUtil.requireForeignIdsAreEqual(impfstoffChargeId, impfstoffChargeService.getById(impfstoffChargeId).getImpfstoff().getId(), expectedImpfstoffId);
    }
}
