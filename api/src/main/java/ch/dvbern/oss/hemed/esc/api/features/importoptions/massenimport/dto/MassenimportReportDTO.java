/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MassenimportReportDTO {

    @NotNull
    private boolean success;

    @NotNull
    private int totalRows;

    @NotNull
    private int skippedRows;

    @NotNull
    private List<KindMassenimportDTO> importedKinder;

    @NotNull
    private List<KontaktPersonKindMassenimportDTO> importedKontaktPersonenKind;

    @NotNull
    private List<MassenimportErrorDTO> errorsWhilePreparing;

    @NotNull
    private List<String> exceptionMessage;

}
