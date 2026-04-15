package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
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
public class KlasseExternalSyncService {

    private final KlasseExternalSyncHelperService klasseExternalSyncHelperService;
    private final ManagedExecutor concurrentExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public KlasseExternalSyncService(
        KlasseExternalSyncHelperService klasseExternalSyncHelperService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentExecutor
    ) {
        this.klasseExternalSyncHelperService = klasseExternalSyncHelperService;
        this.concurrentExecutor = concurrentExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Klasse>> syncKlassen(
        @NonNull ImportPlanDTO<KlasseExternal> importPlan,
        @NonNull ImportTyp importTyp
    ) {
        List<ItemSyncResultJson<Klasse>> addResults = addNewKlassenAsync(importPlan.itemsToAdd(), importTyp);
        List<ItemSyncResultJson<Klasse>> updateResults =
            updateExistingKlassenAsync(importPlan.getItemsToSyncMap(), importTyp);
        List<ItemSyncResultJson<Klasse>> deactivateResults =
            deactivateRemovedKlassenAsync(importPlan.itemsToDelete(), importTyp);

        List<ItemSyncResultJson<Klasse>> klasseImportResults = new ArrayList<>(addResults);
        klasseImportResults.addAll(updateResults);
        klasseImportResults.addAll(deactivateResults);
        return klasseImportResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Klasse>> addNewKlassenAsync(
        @NonNull Set<KlasseExternal> itemsToPersist,
        @NonNull ImportTyp importTyp) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to add {} Klassen", importTyp, itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Klasse>>> futures = itemsToPersist.stream()
            .map(klasseExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return klasseExternalSyncHelperService.addNewKlasse(klasseExternalToAdd, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error adding new Klasse with externalId: '{}'",
                            importTyp,
                            klasseExternalToAdd.getExternalId(), e);
                        return ItemSyncResultJson.<Klasse>createAddErrorItem(
                            klasseExternalToAdd,
                            Objects.toString(
                                klasseExternalToAdd.getKlasseBezeichnung(),
                                klasseExternalToAdd.getExternalId()),
                            e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Klasse>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished persisting new Klassen, took {}ms",
            importTyp,
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Klasse>> deactivateRemovedKlassenAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete,
        @NonNull ImportTyp importTyp) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to deactivate {} Klassen", importTyp, itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Klasse>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return klasseExternalSyncHelperService.deactivateRemovedKlasse(externalSystemIdDTO, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error deactivating Klasse with externalId: '{}', internalId: '{}'",
                            importTyp,
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(externalSystemIdDTO, Klasse.class, e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Klasse>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished deactivating Klassen, took {}ms",
            importTyp,
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Klasse>> updateExistingKlassenAsync(
        @NonNull Map<ExternalSystemIdDTO, KlasseExternal> itemsToSync,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to update {} Klassen", importTyp, itemsToSync.size());

        List<CompletableFuture<ItemSyncResultJson<Klasse>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return klasseExternalSyncHelperService.updateExistingKlasse(idDTO, entry.getValue(),  importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error updating Klasse with externalId: '{}', internalId: '{}'",
                            importTyp,
                            idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(idDTO, Klasse.class, e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Klasse>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished updating Klassen, took {}ms",
            importTyp,
            stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
