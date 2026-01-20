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

package ch.dvbern.oss.hemed.esc.api.shared.externalsystem;

import java.beans.Transient;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Data
public class ItemSyncResultJson<T extends AbstractEntity<T>> {

    @NonNull
    @NotNull
    @Schema(nullable = false)
    private final ImportMode determinedImportMode;

    @NotNull
    @NonNull
    @Valid
    @Schema(nullable = false)
    private final ImportStatus status;

    @NotNull
    @NonNull
    @Schema(nullable = false)
    @IgnoreForIdClassTest
    private final String externalId;

    @Nullable
    @Schema(nullable = true)
    @IgnoreForIdClassTest
    private final ID<T> internalId;

    @Nullable
    @Schema(nullable = true)
    private final String friendlyName;

    @Nullable
    @Schema(nullable = true)
    private final String msg;

    public static final String REDACTION_MSG = "*REDACTED*";

    @Transient
    @JsonIgnore
    public boolean isError() {
        return status == ImportStatus.ERROR;
    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createUpdateErrorItem(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull Class<T> clazz,
        @NonNull Exception e
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.UPDATE,
            ImportStatus.ERROR,
            idDTO.getExternalId(),
            ID.of(idDTO.getInternalId(), clazz),
            null,
            getExceptionMsg(e)
        );
    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createUpdateOkItem(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull Class<T> clazz,
        @Nullable String friendlyName
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.UPDATE,
            ImportStatus.OK,
            idDTO.getExternalId(),
            ID.of(idDTO.getInternalId(), clazz),
            Objects.toString(friendlyName, REDACTION_MSG),
            null
        );
    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createAddOkItem(
        @NonNull HasExternalId externalItemToAdd,
        @NonNull ID<T> internalId,
        @Nullable String friendlyName
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.CREATE,
            ImportStatus.OK,
            externalItemToAdd.getExternalIdNonNull(),
            internalId,
            Objects.toString(friendlyName, REDACTION_MSG),
            null);
    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createAddErrorItem(
        @NonNull HasExternalId externalItem,
        @NonNull String friendlyName,
        @NonNull Exception e
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.CREATE,
            ImportStatus.ERROR,
            externalItem.getExternalIdNonNull(),
            null,
            friendlyName,
            getExceptionMsg(e));
    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createDeactivateErrorItem(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull Class<T> clazz,
        @NonNull Exception e
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.DELETE,
            ImportStatus.ERROR,
            idDTO.getExternalId(),
            ID.of(idDTO.getInternalId(), clazz),
            null,
            getExceptionMsg(e));

    }

    @NonNull
    public static <T extends AbstractEntity<T>> ItemSyncResultJson<T> createDeactivateOkItem(
        @NonNull ExternalSystemIdDTO idDTO,
        @NonNull Class<T> clazz,
        @Nullable String friendlyName
    ) {
        return new ItemSyncResultJson<>(
            ImportMode.DELETE,
            ImportStatus.OK,
            idDTO.getExternalId(),
            ID.of(idDTO.getInternalId(), clazz),
            Objects.toString(friendlyName, REDACTION_MSG),
            null);
    }

    @NonNull
    private static String getExceptionMsg(@NonNull Exception e) {
        return e.getMessage() + " | root cause: " + ExceptionUtils.getRootCauseMessage(e);
    }
}
