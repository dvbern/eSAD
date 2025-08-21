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

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface HasExternalId {

    @Nullable
    String getExternalId();

    @NonNull
    default String getExternalIdNonNull() {
        return Objects.requireNonNull(getExternalId(), "ExternalId must be set");
    }

    Comparator<HasExternalId> EXTERNAL_ID_COMPARATOR = Comparator.comparing(
        hasExternalId ->
            Objects.requireNonNull(hasExternalId.getExternalId(), "ExternalId must be set for unique comparison")
    );

    @NonNull
    static <T extends HasExternalId> Collector<T, ?, TreeSet<T>> toUniqueExternalIdSet() {
        return Collectors.toCollection(() -> new TreeSet<>(HasExternalId.EXTERNAL_ID_COMPARATOR));
    }
}
