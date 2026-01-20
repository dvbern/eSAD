package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
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
public class LehrpersonExternalSyncService {

    private final LehrpersonExternalSyncHelperService syncHelperService;
    private final ManagedExecutor concurrentEscadaExecutor;
    private final ConfigService configService;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public LehrpersonExternalSyncService(
        LehrpersonExternalSyncHelperService syncHelperService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentEscadaExecutor,
        ConfigService configService) {
        this.syncHelperService = syncHelperService;
        this.concurrentEscadaExecutor = concurrentEscadaExecutor;
        this.configService = configService;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Lehrperson>> syncLehrpersonen(
        @NonNull ImportPlanDTO<LehrpersonExternal> importPlan
    ) {
        List<ItemSyncResultJson<Lehrperson>> addResults =
            addNewLehrpersonAsync(importPlan.itemsToAdd());
        List<ItemSyncResultJson<Lehrperson>> updateResults =
            updateExistingLehrpersonenAsync(importPlan.getItemsToSyncMap());
        List<ItemSyncResultJson<Lehrperson>> deactivateResults =
            deactivateRemovedLehrpersonenAsync(importPlan.itemsToDelete());

        List<ItemSyncResultJson<Lehrperson>> importResults = new ArrayList<>(addResults);
        importResults.addAll(updateResults);
        importResults.addAll(deactivateResults);
        return importResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> addNewLehrpersonAsync(
        @NonNull Set<LehrpersonExternal> itemsToPersist
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to add {} KontaktPersonen-Schule", itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToPersist.stream()
            .map(lehrpersonExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.addNewLehrperson(lehrpersonExternalToAdd);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error adding new Lehrperson with externalId: '{}'",
                            lehrpersonExternalToAdd.getExternalId(), e);
                        return ItemSyncResultJson.<Lehrperson>createAddErrorItem(
                            lehrpersonExternalToAdd,
                            Objects.toString(
                                lehrpersonExternalToAdd.getFullNameIfAllowed(configService.isEscadaRedactPersonalInfo()),
                                lehrpersonExternalToAdd.getExternalId()),
                            e);
                    }
                }, concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished persisting new Lehrperson, took {}ms",
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> updateExistingLehrpersonenAsync(
        @NonNull Map<ExternalSystemIdDTO, LehrpersonExternal> itemsToSync
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to update {} Lehrpersonen", itemsToSync.size());
        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return syncHelperService.updateExistingLehrperson(idDTO, entry.getValue());
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error updating Lehrperson with externalId: '{}', internalId: '{}'",
                            idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(
                            idDTO,
                            Lehrperson.class,
                            e);
                    }
                },
                concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished updating Lehrpersonen, took {}ms",
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> deactivateRemovedLehrpersonenAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to deactivate {} Lehrpersonen", itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.deactivateRemovedLehrperson(externalSystemIdDTO);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error deactivating Lehrperson with externalId: '{}', internalId: '{}'",
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(
                            externalSystemIdDTO,
                            Lehrperson.class,
                            e);
                    }
                }, concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished deactivating Lehrpersonen, took {}ms",
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
