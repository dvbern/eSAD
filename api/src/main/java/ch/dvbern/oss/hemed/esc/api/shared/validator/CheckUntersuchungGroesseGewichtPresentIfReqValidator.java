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
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungGroesseGewichtPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungGroesseGewichtPresentIfReq, UntersuchungGroesseGewicht> {

    @Override
    @SuppressWarnings({
        "java:S1066", /* Code is more understandable if statements not merged */
    })
    public boolean isEntityValid(@NonNull UntersuchungGroesseGewicht entity) {
        AnlassConfig anlassConfig = entity.getUntersuchung().getAnlass().getAnlassConfig();
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getGroesseGewichtGroessePerzentile(), entity.getGroessePerzentile(), PerzentileGroesse.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getGroesseGewichtGewichtPerzentile(), entity.getGewichtPerzentile(), PerzentileGewicht.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getGroesseGewichtBmiPerzentile(), entity.getBmiPerzentile(), PerzentileBmi.class)) {
            return false;
        }
        if (anlassConfig.getGroesseGewichtGroesse().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getGroessePerzentile(), PerzentileGroesse.class) != PerzentileGroesse.NICHT_GEPRUEFT) {
                if (entity.getGroesse() == null) {
                    return false;
                }
            }
        }
        if (anlassConfig.getGroesseGewichtGewicht().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getGewichtPerzentile(), PerzentileGewicht.class) != PerzentileGewicht.NICHT_GEPRUEFT) {
                if (entity.getGewicht() == null) {
                    return false;
                }
            }
        }
        if (anlassConfig.getGroesseGewichtBmi().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getBmiPerzentile(), PerzentileBmi.class) != PerzentileBmi.NICHT_GEPRUEFT) {
                if (entity.getBmi() == null) {
                    return false;
                }
            }
        }
        if (anlassConfig.getGroesseGewichtBemerkungen().isRequired()) {
            if (entity.getBemerkungen() == null) {
                return false;
            }
        }
        return true;
    }
}
