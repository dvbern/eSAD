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

import jakarta.enterprise.context.ApplicationScoped;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;

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
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getGroesseGewichtGroessePerzentileSingleValue(),
            entity.getGroessePerzentileSingleValue(),
            PerzentileGroesse.class)) {
            return false;
        }
        if (anlassConfig.getGroesseGewichtGroessePerzentileMultiValue().isRequired()
            && ValueEnhancedUtil.getEnumSetOrEmpty(entity.getGroessePerzentileMultiValue(), PerzentileGroesse.class)
            .isEmpty()) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getGroesseGewichtGewichtPerzentileSingleValue(),
            entity.getGewichtPerzentileSingleValue(),
            PerzentileGewicht.class)) {
            return false;
        }
        if (anlassConfig.getGroesseGewichtGewichtPerzentileMultiValue().isRequired()
            && ValueEnhancedUtil.getEnumSetOrEmpty(entity.getGewichtPerzentileMultiValue(), PerzentileGewicht.class)
            .isEmpty()) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getGroesseGewichtBmiPerzentileSingleValue(),
            entity.getBmiPerzentileSingleValue(),
            PerzentileBmi.class)) {
            return false;
        }
        if (anlassConfig.getGroesseGewichtBmiPerzentileMultiValue().isRequired()
            && ValueEnhancedUtil.getEnumSetOrEmpty(entity.getBmiPerzentileMultiValue(), PerzentileBmi.class)
            .isEmpty()) {
            return false;
        }
        if (anlassConfig.getGroesseGewichtGroesse().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getGroessePerzentileSingleValue(), PerzentileGroesse.class)
                != PerzentileGroesse.NICHT_GEPRUEFT
                && !ValueEnhancedUtil.getEnumSetOrEmpty(
                    entity.getGroessePerzentileMultiValue(),
                    PerzentileGroesse.class)
                .contains(PerzentileGroesse.NICHT_GEPRUEFT)) {
                if (entity.getGroesse() == null) {
                    return false;
                }
            }
        }
        if (anlassConfig.getGroesseGewichtGewicht().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getGewichtPerzentileSingleValue(), PerzentileGewicht.class)
                != PerzentileGewicht.NICHT_GEPRUEFT
                && !ValueEnhancedUtil.getEnumSetOrEmpty(
                    entity.getGewichtPerzentileMultiValue(),
                    PerzentileGewicht.class)
                .contains(PerzentileGewicht.NICHT_GEPRUEFT)) {
                if (entity.getGewicht() == null) {
                    return false;
                }
            }
        }
        if (anlassConfig.getGroesseGewichtBmi().isRequired()) {
            if (ValueEnhancedUtil.getEnumValueOrNull(entity.getBmiPerzentileSingleValue(), PerzentileBmi.class)
                != PerzentileBmi.NICHT_GEPRUEFT
                && !ValueEnhancedUtil.getEnumSetOrEmpty(entity.getBmiPerzentileMultiValue(), PerzentileBmi.class)
                .contains(PerzentileBmi.NICHT_GEPRUEFT)) {
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
