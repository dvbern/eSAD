package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffImpfstoffChargeDataJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class ImpfstoffMapper {

    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final ImpfstoffChargeMapper impfstoffChargeMapper;

    @NonNull
    public static ImpfstoffJson toJson(@NonNull Impfstoff impfstoff) {
        return new ImpfstoffJson(
            impfstoff.getId(),
            new ImpfstoffSaveJson(
                impfstoff.getName(),
                EnumSetUtil.toList(impfstoff.getKrankheiten()),
                impfstoff.getImpfstoffAktivStatus(),
                impfstoff.getVersion()
            )
        );
    }

    @NonNull
    public ImpfstoffImpfstoffChargeDataJson findAllWithChargen() {
        List<ImpfstoffJson> impfstoffe = impfstoffService.findAll().stream()
            .map(ImpfstoffMapper::toJson)
            .toList();
        List<ImpfstoffChargeJson> chargen = impfstoffChargeMapper.findAll();
        return new ImpfstoffImpfstoffChargeDataJson(impfstoffe, chargen);
    }

    @NonNull
    private Impfstoff toEntityForCreate(@NonNull ImpfstoffSaveJson impfstoffSaveJson) {
        Impfstoff impfstoff = new Impfstoff();
        impfstoff.setName(impfstoffSaveJson.getName());
        impfstoff.setKrankheiten(impfstoffSaveJson.getKrankheiten());
        impfstoff.setImpfstoffAktivStatus(impfstoffSaveJson.getImpfstoffAktivStatus());

        return impfstoff;
    }

    @NonNull
    private Impfstoff toEntity(@NonNull Impfstoff existing, @NonNull ImpfstoffSaveJson impfstoffSaveJson) {
        existing.setName(impfstoffSaveJson.getName());
        existing.setKrankheiten(impfstoffSaveJson.getKrankheiten());
        if (existing.getImpfstoffAktivStatus() == EntityAktivStatus.AKTIV
            && impfstoffSaveJson.getImpfstoffAktivStatus() == EntityAktivStatus.INAKTIV) {
            deactivateImpfstoffChargen(existing.getId());
        }
        existing.setImpfstoffAktivStatus(impfstoffSaveJson.getImpfstoffAktivStatus());

        return existing;
    }

    @NonNull
    public ImpfstoffJson create(
        @NonNull ImpfstoffSaveJson impfstoffSaveJson
    ) {
        Impfstoff entity = toEntityForCreate(impfstoffSaveJson);
        return toJson(impfstoffService.persist(entity));
    }

    @NonNull
    public ImpfstoffJson update(@NonNull ID<Impfstoff> impfstoffID, @NonNull ImpfstoffSaveJson impfstoffSaveJson) {
        Impfstoff existing = impfstoffService.getById(impfstoffID);
        Impfstoff entity = toEntity(existing, impfstoffSaveJson);
        return toJson(impfstoffService.persistWithOptimisticLocking(entity, impfstoffSaveJson.getVersion()));
    }

    public ImpfstoffJson changeImpfstoffAktivStatus(
        @NonNull ID<Impfstoff> impfstoffId,
        @NonNull EntityAktivStatusSaveJson saveJson
    ) {
        Impfstoff existing = impfstoffService.getById(impfstoffId);
        if (saveJson.getEntityAktivStatus() == EntityAktivStatus.INAKTIV
            && existing.getImpfstoffAktivStatus() == EntityAktivStatus.AKTIV) {
            deactivateImpfstoffChargen(impfstoffId);
        }
        existing.setImpfstoffAktivStatus(saveJson.getEntityAktivStatus());
        existing.disableOptimisticLockingCheck();
        return toJson(this.impfstoffService.persist(existing));
    }

    private void deactivateImpfstoffChargen(
        @NonNull ID<Impfstoff> impfstoffId
    ) {
        impfstoffChargeMapper.deactivateAllChargenByImpfstoff(impfstoffId);
    }

    public void remove(@NonNull ID<Impfstoff> impfstoffID) {
        impfstoffChargeService.findAllByImpfstoff(impfstoffID)
            .forEach(impfstoff -> impfstoffChargeService.remove(impfstoff.getId()));

        impfstoffService.remove(impfstoffID);
    }
}
