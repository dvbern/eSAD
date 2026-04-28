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
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungHoerenAudiogrammPresentIfReq, UntersuchungHoerenAudiogramm> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungHoerenAudiogramm hoerenAudiogramm) {
        AnlassConfig anlassConfig = hoerenAudiogramm.getUntersuchung().getAnlass().getAnlassConfig();

        if (anlassConfig.getHoerenAudiogramm().isRequired()
            && !ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getAudiogrammBeurteilung(), AudiogrammBeurteilung.class).contains(AudiogrammBeurteilung.NICHT_GEPRUEFT)
            && (hoerenAudiogramm.getAudiogrammRechts() == null || hoerenAudiogramm.getAudiogrammLinks() == null)) {
            return false;
        }
        if ((anlassConfig.getHoerenAudiogramm250Hz().isRequired()
            && !ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getAudiogrammBeurteilung(), AudiogrammBeurteilung.class).contains(AudiogrammBeurteilung.NICHT_GEPRUEFT))
            && ((hoerenAudiogramm.getAudiogrammLinks() == null || hoerenAudiogramm.getAudiogrammRechts() == null)
            || (hoerenAudiogramm.getAudiogrammLinks().getDbHz250() == null
            || hoerenAudiogramm.getAudiogrammRechts().getDbHz250() == null))
        ) {
            return false;
        }
        if ((anlassConfig.getHoerenAudiogramm8000Hz().isRequired()
            && !ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getAudiogrammBeurteilung(), AudiogrammBeurteilung.class).contains(AudiogrammBeurteilung.NICHT_GEPRUEFT))
            && ((hoerenAudiogramm.getAudiogrammLinks() == null || hoerenAudiogramm.getAudiogrammRechts() == null)
            || (hoerenAudiogramm.getAudiogrammLinks().getDbHz8000() == null
            || hoerenAudiogramm.getAudiogrammRechts().getDbHz8000() == null))
        ) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getHoerenAudiogrammSimpel(), hoerenAudiogramm.getAudiogrammSimpel(), AudiogrammSimpel.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getHoerenHoerhilfe(), hoerenAudiogramm.getHoerhilfe(), HoerenHoerhilfe.class)) {
            return false;
        }
        if (anlassConfig.getHoerenAudiogrammBeurteilung().isRequired() && ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getAudiogrammBeurteilung(), AudiogrammBeurteilung.class)
            .isEmpty()) {
            return false;
        }
        if (anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben().isRequired()
            && ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getZusaetzlicheAngaben(), HoerenZusaetzlicheAngaben.class)
            .isEmpty()) {
            return false;
        }
        if (anlassConfig.getHoerenAudiogrammEmpfehlung().isRequired() && CollectionUtils.isEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(hoerenAudiogramm.getEmpfehlung(), HoerenEmpfehlung.class))) {
            return false;
        }
        if (anlassConfig.getHoerenAudiogrammBemerkungen().isRequired() && hoerenAudiogramm.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
