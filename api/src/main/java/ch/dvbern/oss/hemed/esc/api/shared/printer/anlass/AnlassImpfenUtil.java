/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Helper to determine, for a given Einladung, the Impfstoffe of the Impfungen that are currently planned
 * ({@link ImpfStatus#GEPLANT}). Used to list the geplante Impfungen on the "Ausdruck Impfen" print-out.
 */
@UtilityClass
public final class AnlassImpfenUtil {

    @NonNull
    public static List<Impfstoff> getGeplanteImpfstoffe(
        @NonNull Einladung einladung,
        @NonNull UntersuchungImpfungInternService untersuchungImpfungInternService
    ) {
        if (einladung.getUntersuchung() == null) {
            return List.of();
        }

        return untersuchungImpfungInternService.findByUntersuchung(einladung.getUntersuchung().getId()).stream()
            .filter(impfung -> impfung.getImpfStatus() == ImpfStatus.GEPLANT)
            .map(UntersuchungImpfungIntern::getImpfstoff)
            .toList();
    }
}
