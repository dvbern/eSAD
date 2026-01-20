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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class MassenimportResultDTO {

    @NotNull
    private int totalRows;

    @NotNull
    private int skippedRows; // if a row has a Problem that will trigger a rollback it counts as skipped

    @NotNull
    private List<Kind> importedKinder;

    @NotNull
    private List<KontaktPersonKind> importedKontaktPersonenKind;

    @NotNull
    private List<Klasse> importedKlassen;

    @NotNull
    private List<MassenimportErrorDTO> errorsWhilePreparing;
        // contains both, errors that trigger rollback and errors that do not

    @NotNull
    private List<Exception> exceptionsWhilePersisting; // all exceptions trigger a rollback

    public boolean couldImportAllRows() {
        return skippedRows == 0 && exceptionsWhilePersisting.isEmpty();
    }
}
