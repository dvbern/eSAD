/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungImpfungenMassnahmenPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungImpfungenMassnahmenPresentIfReq, UntersuchungImpfungenMassnahmen> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungImpfungenMassnahmen impfungen) {
        // Die Impfungen werden bereits beim ersten Lesen erstellt. Wir können darum beim ersten Speichern
        // noch nicht validieren.
        if (impfungen.getVersion() < 1) {
            return true;
        }
        AnlassConfig anlassConfig = impfungen.getUntersuchungImpfungen().getUntersuchung().getEinladung().getAnlass().getAnlassConfig();
        if (anlassConfig.getUntersuchungImpfungAchtung().isRequired()
                && StringUtils.isEmpty(impfungen.getAchtung())) {
            return false;
        }
        return true;
    }
}
