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

import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungSehenBeurteilungPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungSehenBeurteilungPresentIfReq, UntersuchungSehenBeurteilung> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungSehenBeurteilung sehen) {
        AnlassConfig anlassConfig = sehen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();
        if (anlassConfig.getSehenBrilleLinsen().isRequired() && sehen.getBrilleLinsen() == null) {
            return false;
        }
        if (anlassConfig.getSehenStereoTest().isRequired() && sehen.getStereotest() == null) {
            return false;
        }
        if (anlassConfig.getSehenFarbsehen().isRequired() && sehen.getFarbsehen() == null) {
            return false;
        }
        if (anlassConfig.getSehenTafel().isRequired() && sehen.getVerwendeteTafel() == null) {
            return false;
        }
        if (anlassConfig.getSehenFernvisus().isRequired()) {
            if (CollectionUtils.isEmpty(sehen.getFernvisus())) {
                return false;
            }
            if (!sehen.getFernvisus().contains(DistanzSehenResultat.NICHT_GEPRUEFT)
                && (sehen.getFernvisusLinks() == null || sehen.getFernvisusRechts() == null)
            ) {
                return false;
            }
        }
        if (anlassConfig.getSehenNahvisus().isRequired()) {
            if (CollectionUtils.isEmpty(sehen.getNahvisus())) {
                return false;
            }
            if (!sehen.getNahvisus().contains(DistanzSehenResultat.NICHT_GEPRUEFT)
                && (sehen.getNahvisusLinks() == null || sehen.getNahvisusRechts() == null)
            ) {
                return false;
            }
        }
        if (anlassConfig.getSehenMotilitaet().isRequired() && sehen.getMotilitaet() == null) {
            return false;
        }
        if (anlassConfig.getSehenStrabismus().isRequired()
            && (CollectionUtils.isEmpty(sehen.getStrabismus())
                || sehen.getStrabismusAuspraegung() == null
                || sehen.getStrabismusAuftreten() == null)
        ) {
            return false;
        }
        if (anlassConfig.getSehenInspektion().isRequired() && sehen.getInspektion() == null) {
            return false;
        }
        if (anlassConfig.getSehenKonvergenz().isRequired() && sehen.getKonvergenz() == null) {
            return false;
        }
        if (anlassConfig.getSehenEmpfehlung().isRequired() && CollectionUtils.isEmpty(sehen.getEmpfehlung())) {
            return false;
        }
        if (anlassConfig.getSehenBemerkungen().isRequired() && sehen.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
