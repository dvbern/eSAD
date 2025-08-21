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

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungHoerenOtoskopiePresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungHoerenOtoskopiePresentIfReq, UntersuchungHoerenOtoskopie> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungHoerenOtoskopie hoerenOtoskopie) {
        AnlassConfig anlassConfig = hoerenOtoskopie.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();

        if (anlassConfig.getHoerenOtoskopie().isRequired()) {
            if (hoerenOtoskopie.getOtoskopieRechts().isEmpty()) {
                return false;
            }
            if (hoerenOtoskopie.getOtoskopieLinks().isEmpty()) {
                return false;
            }
            if (hoerenOtoskopie.getOtoskopieRechts().contains(OtoskopieResultat.ANDERES)
                && hoerenOtoskopie.getOtoskopieRechtsResultatAndere() == null) {
                return false;
            }
            if (hoerenOtoskopie.getOtoskopieLinks().contains(OtoskopieResultat.ANDERES)
                && hoerenOtoskopie.getOtoskopieLinksResultatAndere() == null) {
                return false;
            }
        }
        if (anlassConfig.getHoerenOtoskopieBemerkungen().isRequired() && hoerenOtoskopie.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
