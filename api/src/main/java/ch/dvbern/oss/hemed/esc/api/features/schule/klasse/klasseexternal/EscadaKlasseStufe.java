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

package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public enum EscadaKlasseStufe {

    KINDERGARTEN("Kindergarten"),
    PRIMARSCHULE("Primarstufe"),
    SEKUNDARSTUFE("Sekundarstufe"),
    SEKUNDARSTUFE_II("Sekundarstufe II");

    private final String klassenStufeValue;

    @NonNull
    public static EscadaKlasseStufe fromString(@NonNull String value) {
        for (EscadaKlasseStufe stufe : EscadaKlasseStufe.values()) {
            if (stufe.klassenStufeValue.equalsIgnoreCase(value)) {
                return stufe;
            }
        }
        throw new AppFailureException(AppFailureMessage.internalError(
            "Unmapped KlasseStufe from escada: '%s'".formatted(value))
        );
    }
}
