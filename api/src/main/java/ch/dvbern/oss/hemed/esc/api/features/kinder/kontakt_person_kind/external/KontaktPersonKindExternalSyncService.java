package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportPlanDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import io.smallrye.context.api.ManagedExecutorConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.context.ManagedExecutor;

@Service
@Slf4j
public class KontaktPersonKindExternalSyncService {

    private final KontaktPersonKindExternalSyncHelperService syncHelperService;
    private final ConfigService configService;
    private final ManagedExecutor concurrentExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public KontaktPersonKindExternalSyncService(
        KontaktPersonKindExternalSyncHelperService syncHelperService,
        ConfigService configService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentExecutor
    ) {
        this.syncHelperService = syncHelperService;
        this.configService = configService;
        this.concurrentExecutor = concurrentExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<KontaktPersonKind>> syncKontaktPersonKind(
        @NonNull ImportPlanDTO<KontaktPersonKindExternal> importPlan,
        @NonNull ImportTyp importTyp
    ) {
        List<ItemSyncResultJson<KontaktPersonKind>> addResults =
            addNewKontaktPersonenAsync(importPlan.itemsToAdd(), importTyp);
        List<ItemSyncResultJson<KontaktPersonKind>> updateResults =
            updateExistingKontaktPersonenAsync(importPlan.getItemsToSyncMap(), importTyp);
        List<ItemSyncResultJson<KontaktPersonKind>> deactivateResults =
            deactivateRemovedKontaktPersonenAsync(importPlan.itemsToDelete(), importTyp);

        List<ItemSyncResultJson<KontaktPersonKind>> importResults = new ArrayList<>(addResults);
        importResults.addAll(updateResults);
        importResults.addAll(deactivateResults);
        return importResults;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> addNewKontaktPersonenAsync(
        @NonNull Set<KontaktPersonKindExternal> itemsToPersist,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("{}: Starting to add {} KontaktPersonen-Kind", importTyp, itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToPersist.stream()
            .map(kontaktPersonKindExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.addNewKontaktPerson(kontaktPersonKindExternalToAdd, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error adding new KontaktPersonKind with externalId: {}",
                            importTyp, kontaktPersonKindExternalToAdd.getExternalId(), e);
                        return ItemSyncResultJson.<KontaktPersonKind>createAddErrorItem(
                            kontaktPersonKindExternalToAdd,
                            Objects.toString(
                                kontaktPersonKindExternalToAdd.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo()),
                                kontaktPersonKindExternalToAdd.getAdressierungId()),
                            e);
                    }
                }, concurrentExecutor)
            )
            .toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished persisting new KontaktPersonen-Kind, took {}ms",
            importTyp, stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> updateExistingKontaktPersonenAsync(
        @NonNull Map<ExternalSystemIdDTO, KontaktPersonKindExternal> itemsToSync,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("{}: Starting to update {} KontaktPersonen-Kind", importTyp, itemsToSync.size());
        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return syncHelperService.updateExistingKontaktPerson(idDTO, entry.getValue(), importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error updating KontaktPersonKind with externalId: '{}', internalId: '{}'",
                            importTyp, idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(
                            idDTO,
                            KontaktPersonKind.class,
                            e);
                    }
                },
                concurrentExecutor)
            ).toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished updating KontaktPersonen-Kind, took {}ms",
            importTyp, stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> deactivateRemovedKontaktPersonenAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("{}: Starting to deactivate {} KontaktPersonen-Kind", importTyp, itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToDelete.stream()
            .map(
                externalSystemIdDTO -> CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            return syncHelperService.deactivateRemovedKonkaktPersonen(externalSystemIdDTO, importTyp);
                        } catch (Exception e) {
                            LOG.warn(
                                "{}: Error deactivating KontaktPersonKind with externalId: '{}', internalId: '{}'",
                                importTyp, externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                            return ItemSyncResultJson.createDeactivateErrorItem(
                                externalSystemIdDTO,
                                KontaktPersonKind.class,
                                e);
                        }
                    }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished deactivating KontaktPersonen-Kind, took {}ms",
            importTyp, stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
