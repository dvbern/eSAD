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
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;

import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReq, UntersuchungImpfungenNurUnvollstaendige> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungImpfungenNurUnvollstaendige data) {
        AnlassConfig anlassConfig = data.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig();
        if (anlassConfig.getImpfungenNurUnvollstaendige().isRequired()) {
            if (data.getImpfausweisVollstaendig() == null) {
                return false;
            }
            if (data.getImpfausweisVollstaendig() == ImpfausweisVollstaendig.NEIN && data.getImpfausweisGrundUnvollstaendig() == null) {
                return false;
            }
        }
        return true;
    }
}
