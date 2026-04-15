package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
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
public class KindExternalSyncService {

    private final KindExternalSyncHelperService kindExternalSyncHelperService;
    private final ConfigService configService;
    private final ManagedExecutor concurrentExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public KindExternalSyncService(
        KindExternalSyncHelperService kindExternalSyncHelperService,
        ConfigService configService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentExecutor
    ) {
        this.kindExternalSyncHelperService = kindExternalSyncHelperService;
        this.configService = configService;
        this.concurrentExecutor = concurrentExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Kind>> syncKinder(
        @NonNull ImportPlanDTO<KindExternal> importPlan,
        @NonNull ImportTyp importTyp
    ) {
        List<ItemSyncResultJson<Kind>> addResults = addNewKinderAsync(importPlan.itemsToAdd(), importTyp);
        List<ItemSyncResultJson<Kind>> updateResults = updateExistingKinderAsync(importPlan.getItemsToSyncMap(), importTyp);
        List<ItemSyncResultJson<Kind>> deactivateResults = deactivateRemovedKinderAsync(importPlan.itemsToDelete(), importTyp);

        List<ItemSyncResultJson<Kind>> kindImportResults = new ArrayList<>(addResults);
        kindImportResults.addAll(updateResults);
        kindImportResults.addAll(deactivateResults);
        return kindImportResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> addNewKinderAsync(@NonNull Set<KindExternal> itemsToPersist, @NonNull ImportTyp importTyp) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to add {} Kinder", importTyp.toString().toUpperCase(), itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToPersist.stream()
            .map(item -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return kindExternalSyncHelperService.addNewKind(item, importTyp);
                    } catch (Exception e) {
                        LOG.warn("{}: Error adding new Kind with externalId: '{}'", importTyp.toString().toUpperCase(), item.getExternalId(), e);
                        return ItemSyncResultJson.<Kind>createAddErrorItem(
                            item,
                            Objects.toString(item.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo()), item.getExternalId()),
                            e
                        );
                    }
                }, concurrentExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished persisting new Kinder, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> deactivateRemovedKinderAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to deactivate {} Kinder", importTyp, itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return kindExternalSyncHelperService.deactivateRemovedKind(externalSystemIdDTO, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error deactivating Kind with externalId: '{}', internalId: '{}'",
                            importTyp,
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(externalSystemIdDTO, Kind.class, e);
                    }
                }, concurrentExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished deactivating Kinder, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Kind>> updateExistingKinderAsync(
        @NonNull Map<ExternalSystemIdDTO, KindExternal> itemsToSync,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to update {} Kinder", importTyp, itemsToSync.size());

        List<CompletableFuture<ItemSyncResultJson<Kind>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return kindExternalSyncHelperService.updateExistingKind(idDTO, entry.getValue(), importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error updating Kind with externalId: '{}', internalId: '{}'",
                            importTyp,
                            idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(idDTO, Kind.class, e);
                    }
                },
                concurrentExecutor))
            .toList();

        // Wait for all tasks to complete
        List<ItemSyncResultJson<Kind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished updating Kinder, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
