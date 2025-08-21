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

package ch.dvbern.oss.hemed.esc.api.shared.escada;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.MoreCollectors;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ImportPlanDTO<T extends HasExternalId>(

    Set<T> itemsToAdd,
    Set<ExternalSystemIdDTO> itemsToDelete,
    Set<T> itemIdsToSync,
    Set<T> importedItems,
    Set<ExternalSystemIdDTO> existingItems
) {

    @NonNull
    public static <T extends HasExternalId> ImportPlanDTO<T> from(
        @NonNull Set<ExternalSystemIdDTO> existingItems,
        @NonNull Set<T> importedItems
    ) {
        // find newly added Items (e.g. items whose external id is not in the existing items)
        Set<T> itemsToAdd = importedItems.stream()
            .filter(item -> item.getExternalId() != null)
            .filter(item -> existingItems.stream()
                .noneMatch(existingItem -> existingItem.getExternalIdNonNull().equals(item.getExternalId())))
            .collect(Collectors.toSet());

        // find Items that were deleted externally (e.g. items whose external id is not in the imported items anymore)
        Set<ExternalSystemIdDTO> itemsToDelete = existingItems.stream()
            .filter(existingItem -> importedItems.stream()
                .noneMatch(importedItem -> importedItem.getExternalIdNonNull()
                    .equals(existingItem.getExternalIdNonNull())))
            .collect(Collectors.toSet());

        // find Items that we have to update (e.g. items whose external id is in the existing items and in the
        // imported items)
        Set<T> itemsToSync = importedItems.stream()
            .filter(item -> item.getExternalId() != null)
            .filter(item -> existingItems.stream()
                .anyMatch(existingItem -> existingItem.getExternalIdNonNull().equals(item.getExternalId())))
            .collect(Collectors.toSet());

        Validate.isTrue(
            itemsToAdd.size() + itemsToSync.size() == importedItems.size(),
            "The sum of items to add and sync must be equal to the number of imported items");
        return new ImportPlanDTO<>(itemsToAdd, itemsToDelete, itemsToSync, importedItems, existingItems);
    }

    @NonNull
    public Map<ExternalSystemIdDTO, T> getItemsToSyncMap() {
        Map<ExternalSystemIdDTO, T> idsToDataMap = new HashMap<>();
        // iterate all items that we want to sync
        for (T dataFromEscada : itemIdsToSync) {
            String externalId = dataFromEscada.getExternalId();
            Objects.requireNonNull(
                externalId,
                "External ID must be set in EscadaData if we want to sync the data with local db");
            // find the corresponding item in the existingItems
            ExternalSystemIdDTO localItem = existingItems.stream()
                .filter(item -> {
                    String localIdInDb = item.getExternalId();
                    Objects.requireNonNull(localIdInDb, "Existing DB item must have an external ID");
                    return localIdInDb.equals(externalId);
                }).collect(MoreCollectors.onlyElement());
            idsToDataMap.put(localItem, dataFromEscada);
        }
        return idsToDataMap;
    }
}

