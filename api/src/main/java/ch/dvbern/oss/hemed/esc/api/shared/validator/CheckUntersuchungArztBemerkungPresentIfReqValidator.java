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
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungArztBemerkungPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungArztBemerkungPresentIfReq, UntersuchungArztBemerkung> {

    @Override
    @SuppressWarnings("java:S1126" /* readability is better with seprate return*/)
    public boolean isEntityValid(@NonNull UntersuchungArztBemerkung aerztlicheUntersuchung) {
        AnlassConfig anlassConfig =
            aerztlicheUntersuchung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();

        if (anlassConfig.getUntersuchungBemerkungen().isRequired()
            && aerztlicheUntersuchung.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
