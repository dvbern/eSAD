package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeRepo;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class UntersuchungImpfungInternService {

    private final UntersuchungImpfungInternRepo untersuchungImpfungInternRepo;
    private final DossierService dossierService;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final ImpfstoffChargeRepo impfstoffChargeRepo;
    private final EinladungService einladungService;

    @NonNull
    public UntersuchungImpfungIntern persist(@NonNull UntersuchungImpfungIntern impfstatusKrankheit) {
        return persist(impfstatusKrankheit, true);
    }

    @NonNull
    public UntersuchungImpfungIntern persist(@NonNull UntersuchungImpfungIntern impfstatusKrankheit, boolean checkImmunitaeten) {
        untersuchungImpfungInternRepo.persist(impfstatusKrankheit);
        if(checkImmunitaeten){
            checkImmunitaetenForDossier(impfstatusKrankheit);
        }
        return impfstatusKrankheit;
    }

    @NonNull
    public UntersuchungImpfungIntern persistWithOptimisticLocking(
        @NonNull UntersuchungImpfungIntern entity,
        @NonNull Long versionFromFrontend) {
        return persistWithOptimisticLocking(entity, versionFromFrontend, true);
    }

    @NonNull
    public UntersuchungImpfungIntern persistWithOptimisticLocking(
        @NonNull UntersuchungImpfungIntern entity,
        @NonNull Long versionFromFrontend,
        boolean checkImmunitaeten) {
        UntersuchungImpfungIntern persisted =
            untersuchungImpfungInternRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
        if(checkImmunitaeten) {
            checkImmunitaetenForDossier(persisted);
        }
        return persisted;
    }

    private void checkImmunitaetenForDossier(@NonNull UntersuchungImpfungIntern impfungIntern) {
        Dossier dossier = dossierService.getById(impfungIntern.getUntersuchungImpfungen()
            .getUntersuchung()
            .getEinladungRequired()
            .getDossier()
            .getId());
        dossier.disableOptimisticLockingCheck();
        Set<UntersuchungImpfungKrankheitImmunitaet> immunitaeten = impfungIntern.getKrankheitenImmunitaeten();
        if (immunitaeten.stream().anyMatch(i -> i.getKrankheit() == Krankheit.MASERN
            && i.isImmunitaetGenuegend())) {
            dossier.setImmunitaetMasern(true);
            dossierService.persist(dossier);
        }
        if (immunitaeten.stream().anyMatch(i -> i.getKrankheit() == Krankheit.VARIZELLEN
            && i.isImmunitaetGenuegend())) {
            dossier.setImmunitaetVarizellen(true);
            dossierService.persist(dossier);
        }
    }

    @NonNull
    public List<UntersuchungImpfungIntern> findByUntersuchung(@NonNull ID<Untersuchung> untersuchungID) {
        return untersuchungImpfungInternRepo.findByUntersuchung(untersuchungID);
    }

    @NonNull
    public UntersuchungImpfungIntern getById(@NonNull ID<UntersuchungImpfungIntern> id) {
        return untersuchungImpfungInternRepo.getById(id);
    }

    public void remove(@NonNull ID<Anlass> anlassId, @NonNull UntersuchungImpfungIntern impfung) {
        if (impfung.getAnlassImpfungErfasst().getId().equals(anlassId)) {
            untersuchungImpfungInternRepo.remove(impfung.getId());
        } else {
            throw new AppFailureException(AppFailureMessage.impfungInternCannotBeRemoved());
        }
    }

    @NonNull
    public Optional<UntersuchungImpfungIntern> findMostRecentlyChangedByDossier(@NonNull ID<Kind> kindId) {
        return untersuchungImpfungInternRepo.findMostRecentlyChangedByDossier(kindId);
    }

    @NonNull
    public SchedulerProcessingResult deactivateImpfstoffChargeIfNotUsedSince(
        int monthsUnused,
        int batchsize
    ) {
        boolean adHocImpfstoffChargenEnabled = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getAdHocImpfstoffChargenFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
        if (adHocImpfstoffChargenEnabled) {
            List<ImpfstoffCharge> chargenToDeactivate =
                untersuchungImpfungInternRepo.getImpfstoffChargenNotUsedSince(monthsUnused, batchsize);
            for (ImpfstoffCharge chargeToDeactivate : chargenToDeactivate) {
                chargeToDeactivate.setAktiv(false);
                chargeToDeactivate.disableOptimisticLockingCheck();
                impfstoffChargeRepo.persist(chargeToDeactivate);
                LOG.info(
                    "ESC_DEACTIVATE_IMPFSTOFF_CHARGEN: ImpfstoffCharge {} (ID {}) zu Impfstoff {} wurde deaktiviert",
                    chargeToDeactivate.getName(),
                    chargeToDeactivate.getId().getId(),
                    chargeToDeactivate.getImpfstoff().getName());
            }
            return new SchedulerProcessingResult(batchsize, chargenToDeactivate.size(), chargenToDeactivate.size());
        }
        return new SchedulerProcessingResult(batchsize, 0, 0);
    }

    @NonNull
    public SchedulerProcessingResult deleteImpfstoffChargeIfNotReferencedSince(
        int monthsUnreferenced,
        int batchsize
    ) {
        boolean adHocImpfstoffChargenEnabled = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getAdHocImpfstoffChargenFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
        if (adHocImpfstoffChargenEnabled) {
            List<ImpfstoffCharge> chargenToDelete =
                untersuchungImpfungInternRepo.getImpfstoffChargenNotReferencedSince(monthsUnreferenced, batchsize);
            for (ImpfstoffCharge chargeToDelete : chargenToDelete) {
                impfstoffChargeRepo.remove(chargeToDelete.getId());
                LOG.info(
                    "ESC_DELETE_IMPFSTOFF_CHARGEN: ImpfstoffCharge {} (ID {}) zu Impfstoff {} wurde gelöscht",
                    chargeToDelete.getName(), chargeToDelete.getId().getId(), chargeToDelete.getImpfstoff().getName());
            }
            return new SchedulerProcessingResult(batchsize, chargenToDelete.size(), chargenToDelete.size());
        }
        return new SchedulerProcessingResult(batchsize, 0, 0);
    }

    @NonNull
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public List<ID<UntersuchungImpfungIntern>> findAllForCsvReport(@NonNull DateRange zeitraum) {
        List<ID<Einladung>> einladungIdList = einladungService.findAllForCsvImpfstoffeReport(zeitraum);

        List<ID<UntersuchungImpfungIntern>> listAllImpfungenId = new ArrayList<>();
        for (ID<Einladung> einladungId : einladungIdList) {

            List<UntersuchungImpfungIntern> impfungenForKind = findImpfungenInternForKindForCSVReport(einladungId);
            // group the impfungen by impfstoff and status
            SetMultimap<ImpfstoffAndStatusKey, UntersuchungImpfungIntern> impfungenByImpfstoffAndStatus =
                createImpfungenMultimap(impfungenForKind);

            for (Entry<ImpfstoffAndStatusKey, Collection<UntersuchungImpfungIntern>> entry : impfungenByImpfstoffAndStatus.asMap().entrySet()) {
                if (!entry.getValue().isEmpty() && entry.getKey().impfStatus == ImpfStatus.GEPLANT) {
                    // take the first of the GEPLANT impfungen
                    listAllImpfungenId.add(entry.getValue().iterator().next().getId());
                } else {
                    // take all of the other impfungen
                    listAllImpfungenId.addAll(entry.getValue().stream().map(UntersuchungImpfungIntern::getId).toList());
                }
            }
        }

        return listAllImpfungenId;
    }

    private SetMultimap<ImpfstoffAndStatusKey, UntersuchungImpfungIntern> createImpfungenMultimap(
        List<UntersuchungImpfungIntern> impfungen
    ) {
        Comparator<UntersuchungImpfungIntern> byDatumImpfungNullsLast =
            Comparator.comparing(
                UntersuchungImpfungIntern::getDatumImpfung, Comparator.nullsLast(Comparator.naturalOrder())

            ).thenComparing(
                Comparator.comparing(
                    ui -> ui.getAnlassImpfungErfasst().getDatum(),
                    Comparator.nullsLast(Comparator.nullsLast(Comparator.naturalOrder())))
            );

        SetMultimap<ImpfstoffAndStatusKey, UntersuchungImpfungIntern> multimap =
            MultimapBuilder.hashKeys().treeSetValues(byDatumImpfungNullsLast).build();

        for (UntersuchungImpfungIntern impfung : impfungen) {
            ImpfstoffAndStatusKey key = new ImpfstoffAndStatusKey(
                impfung.getImpfstoff().getId(),
                impfung.getImpfStatus()
            );
            multimap.put(key, impfung);
        }

        return multimap;
    }

    private record ImpfstoffAndStatusKey(
        ID<Impfstoff> impfstoffId,
        ImpfStatus impfStatus
    ) {}

    private List<UntersuchungImpfungIntern> findImpfungenInternForKindForCSVReport(ID<Einladung> einladungId) {
        List<UntersuchungImpfungIntern> impfungenByEinladung = untersuchungImpfungInternRepo.findByEinladungWithFetchedAnlass(einladungId);
        if (impfungenByEinladung.isEmpty()) {
            // Es gibt in der letzen Untersuchung keine Impfungen für dieses Kind
            Einladung einladung = einladungService.getById(einladungId);
            return untersuchungImpfungInternRepo.findLastImpfungenBeforeEinladung(einladung);
        }
        return impfungenByEinladung;
    }
}
