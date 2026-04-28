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

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungImpfungenPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungImpfungenPresentIfReq, UntersuchungImpfungen> {

    @Override
    public boolean isEntityValid(@NonNull UntersuchungImpfungen impfungen) {
        // Die Impfungen werden bereits beim ersten Lesen erstellt. Wir können darum beim ersten Speichern
        // noch nicht validieren.
        if (impfungen.getVersion() < 1) {
            return true;
        }
        AnlassConfig anlassConfig = impfungen.getUntersuchung().getAnlass().getAnlassConfig();
        if (anlassConfig.getCheckEinwilligungVorhanden().isRequired()
                && !impfungen.isCheckEinwilligungVorhanden()) {
            return false;
        }
        if (anlassConfig.getCheckKontraindikationenAbgeklaert().isRequired()
                && !impfungen.isCheckKontraindikationenAbgeklaert()) {
            return false;
        }
        if (anlassConfig.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt().isRequired()
                && !impfungen.isCheckDoppelkontrolleImpfentscheidDurchgefuehrt()) {
            return false;
        }
        if (anlassConfig.getUntersuchungImpfungBemerkungen().isRequired()
                && StringUtils.isEmpty(impfungen.getBemerkungen())) {
            return false;
        }
        return true;
    }
}
