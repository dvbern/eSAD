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

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungLaeusekontrolleValidator
    extends AbstractValidator<CheckUntersuchungLaeusekontrollePresentIfReq, UntersuchungLaeusekontrolle> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungLaeusekontrolle instance) {
        AnlassConfig anlassConfig =
            instance.getUntersuchung().getAnlass().getAnlassConfig();
        if (anlassConfig.getLaeusekontrolleBeurteilung().isRequired() && CollectionUtils.isEmpty(instance.getBeurteilung())) {
            return false;
        }
        if (anlassConfig.getLaeusekontrolleBemerkungen().isRequired() && StringUtils.isEmpty(instance.getBemerkungen())) {
            return false;
        }
        return true;
    }
}
