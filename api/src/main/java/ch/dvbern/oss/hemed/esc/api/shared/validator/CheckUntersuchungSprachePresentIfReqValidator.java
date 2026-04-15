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
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungSprachePresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungSprachePresentIfReq, UntersuchungSprache> {

    @Override
    @SuppressWarnings({
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungSprache untersuchungSprache) {
        AnlassConfig anlassConfig = untersuchungSprache.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungSpracheNachsprechen(), untersuchungSprache.getNachsprechen(), Nachsprechen.class)) {
              return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungSpracheWortschatz(), untersuchungSprache.getWortschatz(), Wortschatz.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getUntersuchungSpracheSprachverstaendnis(), untersuchungSprache.getSprachverstaendnis(), Sprachverstaendnis.class)) {
            return false;
        }
        if (anlassConfig.getUntersuchungSpracheBemerkungen().isRequired() && untersuchungSprache.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
