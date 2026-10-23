package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImpfstoffChargeService {

    private final ImpfstoffChargeRepo impfstoffChargeRepo;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;


    @NonNull
    public ImpfstoffCharge persist(@NonNull ImpfstoffCharge charge) {
        impfstoffChargeRepo.persist(charge);
        return charge;
    }

    @NonNull
    public ImpfstoffCharge persistWithOptimisticLocking(@NonNull ImpfstoffCharge charge, @NonNull Long versionFromFrontend) {
        impfstoffChargeRepo.persistWithOptimisticLocking(charge, versionFromFrontend);
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

    @NonNull
    public Optional<ImpfstoffCharge> findByImpfstoffAndName(@NonNull ID<Impfstoff> impfstoffId, @NonNull String chargeName) {
        return impfstoffChargeRepo.findByImpfstoffAndName(impfstoffId, chargeName);
    }

    public void remove(@NonNull ID<ImpfstoffCharge> impfstoffChargeId) {
        impfstoffChargeRepo.remove(impfstoffChargeId);
    }

    @Nullable
    public ImpfstoffCharge getChargeOrCreateNewIfFeatureEnabled(@Nullable String chargeName, @NonNull Impfstoff impfstoff) {
        if (chargeName == null) {
            return null;
        }
        Optional<ImpfstoffCharge> byImpfstoffAndName =
            findByImpfstoffAndName(impfstoff.getId(), chargeName);
        boolean adHocImpfstoffChargenEnabled = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getAdHocImpfstoffChargenFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());

        if (byImpfstoffAndName.isPresent()) {
            ImpfstoffCharge existing = byImpfstoffAndName.get();
            if (adHocImpfstoffChargenEnabled) {
                existing.disableOptimisticLockingCheck();
                existing.setAktiv(true);
            }
            return existing;
        }
        // Falls erlaubt: Eine neue Charge anlegen
        if (adHocImpfstoffChargenEnabled) {
            return persist(new ImpfstoffCharge(impfstoff, chargeName, true));
        }
        throw new AppValidationException(AppValidationMessage.invalidImpfstoffCharge(chargeName));
    }
}
