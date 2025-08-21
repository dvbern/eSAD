package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
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
public class KontaktPersonKindExternalSyncService {

    private final KontaktPersonKindExternalSyncHelperService syncHelperService;
    private final ManagedExecutor concurrentEscadaExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public KontaktPersonKindExternalSyncService(
        KontaktPersonKindExternalSyncHelperService syncHelperService,
        @ManagedExecutorConfig(maxAsync = 9) ManagedExecutor concurrentEscadaExecutor
    ) {
        this.syncHelperService = syncHelperService;
        this.concurrentEscadaExecutor = concurrentEscadaExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<KontaktPersonKind>> syncKontaktPersonKind(
        @NonNull ImportPlanDTO<KontaktPersonKindExternal> importPlan
    ) {
        List<ItemSyncResultJson<KontaktPersonKind>> addResults =
            addNewKontaktPersonenAsync(importPlan.itemsToAdd());
        List<ItemSyncResultJson<KontaktPersonKind>> updateResults =
            updateExistingKontaktPersonenAsync(importPlan.getItemsToSyncMap());
        List<ItemSyncResultJson<KontaktPersonKind>> deactivateResults =
            deactivateRemovedKontaktPersonenAsync(importPlan.itemsToDelete());

        List<ItemSyncResultJson<KontaktPersonKind>> importResults = new ArrayList<>(addResults);
        importResults.addAll(updateResults);
        importResults.addAll(deactivateResults);
        return importResults;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> addNewKontaktPersonenAsync(
        @NonNull Set<KontaktPersonKindExternal> itemsToPersist
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to add {} KontaktPersonen-Kind", itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToPersist.stream()
            .map(kontaktPersonKindExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return syncHelperService.addNewKontaktPerson(kontaktPersonKindExternalToAdd);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error adding new KontaktPersonKind with externalId: {}",
                            kontaktPersonKindExternalToAdd.getExternalId(), e);
                        return ItemSyncResultJson.<KontaktPersonKind>createAddErrorItem(
                            kontaktPersonKindExternalToAdd,
                            Objects.toString(
                                kontaktPersonKindExternalToAdd.getAdressierungName() + " " +
                                    kontaktPersonKindExternalToAdd.getAdressierungVorname(),
                                kontaktPersonKindExternalToAdd.getAdressierungId()),
                            e);
                    }
                }, concurrentEscadaExecutor)
            )
            .toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished persisting new KontaktPersonen-Kind, took {}ms",
            stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> updateExistingKontaktPersonenAsync(
        @NonNull Map<ExternalSystemIdDTO, KontaktPersonKindExternal> itemsToSync
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to update {} KontaktPersonen-Kind", itemsToSync.size());
        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return syncHelperService.updateExistingKontaktPerson(idDTO, entry.getValue());
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error updating KontaktPersonKind with externalId: '{}', internalId: '{}'",
                            idDTO.getExternalId(), idDTO.getInternalId(), e);
                        return ItemSyncResultJson.createUpdateErrorItem(
                            idDTO,
                            KontaktPersonKind.class,
                            e);
                    }
                },
                concurrentEscadaExecutor)
            ).toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished updating KontaktPersonen-Kind, took {}ms",
            stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<KontaktPersonKind>> deactivateRemovedKontaktPersonenAsync(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete
    ) {
        StopWatch stopWatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to deactivate {} KontaktPersonen-Kind", itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<KontaktPersonKind>>> futures = itemsToDelete.stream()
            .map(
                externalSystemIdDTO -> CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            return syncHelperService.deactivateRemovedKonkaktPersonen(externalSystemIdDTO);
                        } catch (Exception e) {
                            LOG.warn(
                                "ESCADA: Error deactivating KontaktPersonKind with externalId: '{}', internalId: '{}'",
                                externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                            return ItemSyncResultJson.createDeactivateErrorItem(
                                externalSystemIdDTO,
                                KontaktPersonKind.class,
                                e);
                        }
                    }, concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<KontaktPersonKind>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info(
            "ESCADA: Finished deactivating KontaktPersonen-Kind, took {}ms",
            stopWatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
