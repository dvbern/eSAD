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
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
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
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenBrilleLinsen(),
            sehen.getBrilleLinsen(),
            BrilleLinsenResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenStereoTest(),
            sehen.getStereotest(),
            StereotestResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenFarbsehen(),
            sehen.getFarbsehen(),
            FarbsehenResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenTafel(),
            sehen.getVerwendeteTafel(),
            VerwendetTafelResultat.class)) {
            return false;
        }
        if (anlassConfig.getSehenFernvisus().isRequired()) {
            if (CollectionUtils.isEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
                sehen.getFernvisus(),
                DistanzSehenResultat.class))) {
                return false;
            }
            if (!ValueEnhancedUtil.getEnumSetOrEmpty(sehen.getFernvisus(), DistanzSehenResultat.class)
                .contains(DistanzSehenResultat.NICHT_GEPRUEFT)
                && (sehen.getFernvisusLinks() == null || sehen.getFernvisusRechts() == null)
            ) {
                return false;
            }
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenFernvisusAugeEinzeln(),
            sehen.getFernvisusAugeLinks(),
            DistanzSehenAugeEinzelnResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenFernvisusAugeEinzeln(),
            sehen.getFernvisusAugeRechts(),
            DistanzSehenAugeEinzelnResultat.class)) {
            return false;
        }
        if (anlassConfig.getSehenNahvisus().isRequired()) {
            if (CollectionUtils.isEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
                sehen.getNahvisus(),
                DistanzSehenResultat.class))) {
                return false;
            }
            if (!ValueEnhancedUtil.getEnumSetOrEmpty(sehen.getNahvisus(), DistanzSehenResultat.class)
                .contains(DistanzSehenResultat.NICHT_GEPRUEFT)
                && (sehen.getNahvisusLinks() == null || sehen.getNahvisusRechts() == null)
            ) {
                return false;
            }
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenMotilitaet(),
            sehen.getMotilitaet(),
            MotilitaetResultat.class)) {
            return false;
        }
        if (anlassConfig.getSehenStrabismus().isRequired()
            && (CollectionUtils.isEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
            sehen.getStrabismus(),
            StrabismusResultat.class)))
        ) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenStrabismusAuspraegung(),
            sehen.getStrabismusAuspraegung(),
            StrabismusAuspraegungResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenStrabismusAuftreten(),
            sehen.getStrabismusAuftreten(),
            StrabismusAuftretenResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenInspektion(),
            sehen.getInspektion(),
            InspektionKonvergenzResultat.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(
            anlassConfig.getSehenKonvergenz(),
            sehen.getKonvergenz(),
            InspektionKonvergenzResultat.class)) {
            return false;
        }
        if (anlassConfig.getSehenEmpfehlung().isRequired()
            && CollectionUtils.isEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
            sehen.getEmpfehlung(),
            SehenEmpfehlung.class))) {
            return false;
        }
        if (anlassConfig.getSehenBemerkungen().isRequired() && sehen.getBemerkungen() == null) {
            return false;
        }
        return true;
    }
}
