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
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
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
public class SchuleExternalSyncService {

    private final SchuleExternalSyncHelperService schuleExternalSyncHelperService;
    private final ManagedExecutor concurrentExecutor;

    // IMPORANT: for the @ManagedExecutorConfig annotation to work, it must be directly on the @Inject.
    // do NOT use private final quarkus magic or you will exhaust all db connections
    @Inject
    public SchuleExternalSyncService(
        SchuleExternalSyncHelperService schuleExternalSyncHelperService,
        @ManagedExecutorConfig(maxAsync = 9)
        ManagedExecutor concurrentExecutor
    ) {
        this.schuleExternalSyncHelperService = schuleExternalSyncHelperService;
        this.concurrentExecutor = concurrentExecutor;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<ItemSyncResultJson<Schule>> syncSchulen(
        @NonNull ImportPlanDTO<SchuleExternal> importPlan,
        @NonNull ImportTyp importTyp
    ) {
        List<ItemSyncResultJson<Schule>> addResults = addNewSchulen(importPlan.itemsToAdd(), importTyp);
        List<ItemSyncResultJson<Schule>> updateResults = updateExistingSchulen(importPlan.getItemsToSyncMap(), importTyp);
        List<ItemSyncResultJson<Schule>> deactivateResults = deactivateRemovedSchulen(importPlan.itemsToDelete(), importTyp);

        List<ItemSyncResultJson<Schule>> schulImportResults = new ArrayList<>(addResults);
        schulImportResults.addAll(updateResults);
        schulImportResults.addAll(deactivateResults);
        return schulImportResults;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> addNewSchulen(
        @NonNull Set<SchuleExternal> itemsToPersist,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to add {} Schulen", importTyp, itemsToPersist.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToPersist.stream()
            .map(schuleExternalToAdd -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return schuleExternalSyncHelperService.addNewSchule(schuleExternalToAdd, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error adding new Schule with externalId: '{}'",
                            importTyp,
                            schuleExternalToAdd.getExternalId(),
                            e);
                        return ItemSyncResultJson.<Schule>createAddErrorItem(
                            schuleExternalToAdd,
                            Objects.toString(
                                schuleExternalToAdd.getSchulhausBezeichnung(),
                                schuleExternalToAdd.getExternalId()),
                            e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished persisting new Schulen, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> deactivateRemovedSchulen(
        @NonNull Set<ExternalSystemIdDTO> itemsToDelete,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to deactivate {} Schulen", importTyp, itemsToDelete.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToDelete.stream()
            .map(externalSystemIdDTO -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return schuleExternalSyncHelperService.deactivateRemovedSchule(externalSystemIdDTO, importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error deactivating Schule with externalId: '{}', internalId: '{}'",
                            importTyp,
                            externalSystemIdDTO.getExternalId(), externalSystemIdDTO.getInternalId(), e);
                        return ItemSyncResultJson.createDeactivateErrorItem(externalSystemIdDTO, Schule.class, e);
                    }
                }, concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished deactivating Schulen, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }

    @NonNull
    private List<ItemSyncResultJson<Schule>> updateExistingSchulen(
        @NonNull Map<ExternalSystemIdDTO, SchuleExternal> itemsToSync,
        @NonNull ImportTyp importTyp
    ) {
        var stopwatch = StopWatch.createStarted();
        LOG.info("{}: Starting to update {} Schulen", importTyp, itemsToSync.size());

        List<CompletableFuture<ItemSyncResultJson<Schule>>> futures = itemsToSync.entrySet().stream()
            .map(entry -> CompletableFuture.supplyAsync(
                () -> {
                    ExternalSystemIdDTO idDTO = entry.getKey();
                    try {
                        return schuleExternalSyncHelperService.updateExistingSchule(entry.getKey(), entry.getValue(), importTyp);
                    } catch (Exception e) {
                        LOG.warn(
                            "{}: Error updating Schule with externalId: '{}', internalId: '{}'",
                            importTyp,
                            idDTO.getExternalId(),
                            idDTO.getInternalId(),
                            e);
                        return ItemSyncResultJson.createUpdateErrorItem(idDTO, Schule.class, e);
                    }
                },
                concurrentExecutor))
            .toList();

        List<ItemSyncResultJson<Schule>> resultDtos = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        LOG.info("{}: Finished updating Schulen, took {}ms", importTyp, stopwatch.getTime(TimeUnit.MILLISECONDS));
        return resultDtos;
    }
}
