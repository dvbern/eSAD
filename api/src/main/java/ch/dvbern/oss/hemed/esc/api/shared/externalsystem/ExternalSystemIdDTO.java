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

import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;

@Data
public class ExternalSystemIdDTO implements HasExternalId {


    @NotNull
    @NonNull
    @IgnoreForIdClassTest
    private String externalId;

    @NotNull
    @NonNull
    @IgnoreForIdClassTest
    private UUID internalId;

    @QueryProjection
    public ExternalSystemIdDTO(@NonNull String externalId, @NonNull UUID internalId) {
        this.externalId = externalId;
        this.internalId = internalId;
    }
}
