/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungImpfungInternValidator
    extends AbstractValidator<CheckUntersuchungImpfungIntern, UntersuchungImpfungIntern> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungImpfungIntern instance) {
        AnlassConfig anlassConfig =
            instance.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig();
        if (anlassConfig.getImpfungenListe() == RequirementSetting.REQUIRED) {
            if (instance.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT) {
                return instance.getDatumImpfung() != null
                    && instance.getCharge() != null
                    && instance.getUserDurchgefuehrt() != null;
            }
        }
        return true;
    }
}
