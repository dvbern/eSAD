package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
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
    private final ManagedExecutor concurrentExecutor;
    private final ConfigService configService;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public LehrpersonExternalSyncService(
        LehrpersonExternalSyncHelperService syncHelperService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentExecutor,
        ConfigService configService
    ) {
        this.syncHelperService = syncHelperService;
        this.concurrentExecutor = concurrentExecutor;
        this.configService = configService;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Lehrperson>> syncLehrpersonen(
        @NonNull ImportPlanDTO<LehrpersonExternal> importPlan,
        @NonNull ImportTyp importTyp
    ) {
        List<ItemSyncResultJson<Lehrperson>> addResults =
            addNewLehrpersonAsync(importPlan.itemsToAdd(), importTyp);
        List<ItemSyncResultJson<Lehrperson>> updateResults =
            updateExistingLehrpersonenAsync(importPlan.getItemsToSyncMap(), importTyp);
        List<ItemSyncResultJson<Lehrperson>> deactivateResults =
            deactivateRemovedLehrpersonenAsync(importPlan.itemsToDelete(), importTyp);

        List<ItemSyncResultJson<Lehrperson>> importResults = new ArrayList<>(addResults);
        importResults.addAll(updateResults);
        importResults.addAll(deactivateResults);
        return importResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> addNewLehrpersonAsync(
        @NonNull Set<LehrpersonExternal> itemsToPersist,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to add {} KontaktPersonen-Schule", importTyp, itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToPersist.stream()
            .map(lehrpersonExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.addNewLehrperson(lehrpersonExternalToAdd, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error adding new Lehrperson with externalId: '{}'",
                            importTyp, lehrpersonExternalToAdd.getExternalId(), e);
                        return ItemSyncResultJson.<Lehrperson>createAddErrorItem(
                            lehrpersonExternalToAdd,
                            Objects.toString(
                                lehrpersonExternalToAdd.getFullNameIfAllowed(configService.isExternalImportRedactPersonalInfo()),
                                lehrpersonExternalToAdd.getExternalId()),
                            e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished persisting new Lehrperson, took {}ms",
            importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> updateExistingLehrpersonenAsync(
        @NonNull Map<ExternalSystemIdDTO, LehrpersonExternal> itemsToSync,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to update {} Lehrpersonen", importTyp, itemsToSync.size());
        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return syncHelperService.updateExistingLehrperson(idDTO, entry.getValue(), importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error updating Lehrperson with externalId: '{}', internalId: '{}'",
                            importTyp, idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(
                            idDTO,
                            Lehrperson.class,
                            e);
                    }
                },
                concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished updating Lehrpersonen, took {}ms",
            importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Lehrperson>> deactivateRemovedLehrpersonenAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete,
        @NonNull ImportTyp importTyp
    ) {
        StopWatch stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to deactivate {} Lehrpersonen", importTyp, itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Lehrperson>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.deactivateRemovedLehrperson(externalSystemIdDTO, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error deactivating Lehrperson with externalId: '{}', internalId: '{}'",
                            importTyp, externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(
                            externalSystemIdDTO,
                            Lehrperson.class,
                            e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Lehrperson>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "{}: Finished deactivating Lehrpersonen, took {}ms",
            importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
