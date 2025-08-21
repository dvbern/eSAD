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

package ch.dvbern.oss.hemed.esc.api.shared.pagination;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
public class FilterFieldJson<T extends Enum<?>> {

    @Schema(required = true)
    @NonNull
    T columnName;

    /**
     * one or multiple values of any type (string | date | dateRange | List<string> …) (needs to be parsed depending on
     * the filterMethod and the multiValueMethod)
     */
    @Schema(required = true)
    @NonNull
    String filterValue;

    /**
     * examples:
     * - name search field -> AND
     * - multiselect -> OR
     * - select -> SINGLE
     */
    @Schema(required = true)
    @NonNull
    FilterCombination filterCombinationMethod;
}
