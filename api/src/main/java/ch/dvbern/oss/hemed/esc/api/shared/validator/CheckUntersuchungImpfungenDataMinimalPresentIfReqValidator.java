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
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungImpfungenDataMinimalPresentIfReq, UntersuchungImpfungenDataMinimal> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungImpfungenDataMinimal dataMinimal) {
        AnlassConfig anlassConfig = dataMinimal.getUntersuchungImpfungen().getUntersuchung().getEinladung().getAnlass().getAnlassConfig();
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalImpfausweisGeprueft(), dataMinimal.getImpfausweisGeprueft(), ImpfausweisGeprueft.class)){
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalImmunsuppression(),dataMinimal.getImmunsuppression(), Immunsuppression.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalMasernImpfstatus(),dataMinimal.getMasernImpfstatus(), ImpfStatusMinimal.class)) {
            return false;
        }
        if (anlassConfig.getImpfungenDataMinimalMasernAnzahlImpfungen().isRequired()
            && dataMinimal.getMasernAnzahlImpfungen() == null) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalHpvImpfstatus(), dataMinimal.getHpvImpfstatus(), ImpfStatusMinimal.class)) {
            return false;
        }
        if (anlassConfig.getImpfungenDataMinimalHpvAnzahlImpfungen().isRequired()
            && dataMinimal.getHpvAnzahlImpfungen() == null) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalImpfempfehlung(), dataMinimal.getImpfempfehlung(),ImpfempfehlungMinimal.class)) {
            return false;
        }
        if (ValueEnhancedUtil.isRequiredAndNull(anlassConfig.getImpfungenDataMinimalImpfungDurchgefuehrt(),dataMinimal.getImpfungDurchgefuehrt(),ImpfungDurchgefuehrtMinimal.class)) {
            return false;
        }
        return true;
    }
}
