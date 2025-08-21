package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ImportPlanDTO;
import ch.dvbern.oss.hemed.esc.api.shared.escada.ItemSyncResultJson;
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
public class KindExternalSyncService {

    private final KindExternalSyncHelperService kindExternalSyncHelperService;
    private final ManagedExecutor concurrentEscadaExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public KindExternalSyncService(
        KindExternalSyncHelperService kindExternalSyncHelperService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentEscadaExecutor
    ) {
        this.kindExternalSyncHelperService = kindExternalSyncHelperService;
        this.concurrentEscadaExecutor = concurrentEscadaExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Kind>> syncKinder(@NonNull ImportPlanDTO<KindExternal> importPlan) {
        List<ItemSyncResultJson<Kind>> addResults = addNewKinderAsync(importPlan.itemsToAdd());
        List<ItemSyncResultJson<Kind>> updateResults = updateExistingKinderAsync(importPlan.getItemsToSyncMap());
        List<ItemSyncResultJson<Kind>> deactivateResults = deactivateRemovedKinderAsync(importPlan.itemsToDelete());

        List<ItemSyncResultJson<Kind>> kindImportResults = new ArrayList<>(addResults);
        kindImportResults.addAll(updateResults);
        kindImportResults.addAll(deactivateResults);
        return kindImportResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> addNewKinderAsync(@NonNull Set<KindExternal> itemsToPersist) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to add {} Kinder", itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToPersist.stream()
            .map(item -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return kindExternalSyncHelperService.addNewKind(item);
                    } catch (Exception e) {
                        LOG.warn("ESCADA: Error adding new Kind with externalId: '{}'", item.getExternalId(), e);
                        return ItemSyncResultJson.<Kind>createAddErrorItem(
                            item,
                            Objects.toString(item.getFullName(), item.getExternalId()),
                            e
                        );
                    }
                }, concurrentEscadaExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished persisting new Kinder, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> deactivateRemovedKinderAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to deactivate {} Kinder", itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return kindExternalSyncHelperService.deactivateRemovedKind(externalSystemIdDTO);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error deactivating Kind with externalId: '{}', internalId: '{}'",
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(externalSystemIdDTO, Kind.class, e);
                    }
                }, concurrentEscadaExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished deactivating Kinder, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> updateExistingKinderAsync(
        @NonNull Map<ExternalSystemIdDTO, KindExternal> itemsToSync
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to update {} Kinder", itemsToSync.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return kindExternalSyncHelperService.updateExistingKind(idDTO, entry.getValue());
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error updating Kind with externalId: '{}', internalId: '{}'",
                            idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(idDTO, Kind.class, e);
                    }
                },
                concurrentEscadaExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished updating Kinder, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
