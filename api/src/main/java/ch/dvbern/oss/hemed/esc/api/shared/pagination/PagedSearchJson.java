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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class PagedSearchJson<T extends Enum<?>> {

    protected PagedSearchJson(
        @NonNull PageRequestJson paginationRequest,
        @NonNull List<SortJson<T>> sorts,
        @NonNull List<FilterFieldJson<T>> filters
    ) {
        this.paginationRequest = paginationRequest;
        this.sorts = sorts;
        this.filters = filters;
    }

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private PageRequestJson paginationRequest = new PageRequestJson(1, 10, true);

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<SortJson<T>> sorts = new LinkedList<>();

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FilterFieldJson<T>> filters = new LinkedList<>();

}
