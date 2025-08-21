/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
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
public class SchuleExternalSyncService {

    private final SchuleExternalSyncHelperService schuleExternalSyncHelperService;
    private final ManagedExecutor concurrentEscadaExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public SchuleExternalSyncService(
        SchuleExternalSyncHelperService schuleExternalSyncHelperService,
        @ManagedExecutorConfig(maxAsync = 9)
        ManagedExecutor concurrentEscadaExecutor
    ) {
        this.schuleExternalSyncHelperService = schuleExternalSyncHelperService;
        this.concurrentEscadaExecutor = concurrentEscadaExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Schule>> syncSchulen(@NonNull ImportPlanDTO<SchuleExternal> importPlan) {
        List<ItemSyncResultJson<Schule>> addResults = addNewSchulen(importPlan.itemsToAdd());
        List<ItemSyncResultJson<Schule>> updateResults = updateExistingSchulen(importPlan.getItemsToSyncMap());
        List<ItemSyncResultJson<Schule>> deactivateResults = deactivateRemovedSchulen(importPlan.itemsToDelete());

        List<ItemSyncResultJson<Schule>> schulImportResults = new ArrayList<>(addResults);
        schulImportResults.addAll(updateResults);
        schulImportResults.addAll(deactivateResults);
        return schulImportResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> addNewSchulen(
        @NonNull Set<SchuleExternal> itemsToPersist
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to add {} Schulen", itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToPersist.stream()
            .map(schuleExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return schuleExternalSyncHelperService.addNewSchule(schuleExternalToAdd);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error adding new Schule with externalId: '{}'",
                            schuleExternalToAdd.getExternalId(),
                            e);
                        return ItemSyncResultJson.<Schule>createAddErrorItem(
                            schuleExternalToAdd,
                            Objects.toString(
                                schuleExternalToAdd.getSchulhausBezeichnung(),
                                schuleExternalToAdd.getExternalId()),
                            e);
                    }
                }, concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished persisting new Schulen, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> deactivateRemovedSchulen(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to deactivate {} Schulen", itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return schuleExternalSyncHelperService.deactivateRemovedSchule(externalSystemIdDTO);
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error deactivating Schule with externalId: '{}', internalId: '{}'",
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(externalSystemIdDTO, Schule.class, e);
                    }
                }, concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished deactivating Schulen, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> updateExistingSchulen(
        @NonNull Map<ExternalSystemIdDTO, SchuleExternal> itemsToSync
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("ESCADA: Starting to update {} Schulen", itemsToSync.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return schuleExternalSyncHelperService.updateExistingSchule(entry.getKey(), entry.getValue());
                    } catch (Exception e) {
                        LOG.warn(
                            "ESCADA: Error updating Schule with externalId: '{}', internalId: '{}'",
                            idDTO.getExternalId(),
                            idDTO.getInternalId(),
                            e);
                        return ItemSyncResultJson.createUpdateErrorItem(idDTO, Schule.class, e);
                    }
                },
                concurrentEscadaExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("ESCADA: Finished updating Schulen, took {}ms", stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
