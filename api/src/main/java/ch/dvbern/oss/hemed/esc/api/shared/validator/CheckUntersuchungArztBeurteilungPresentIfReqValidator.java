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

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class CheckUntersuchungArztBeurteilungPresentIfReqValidator
    extends AbstractValidator<CheckUntersuchungArztBeurteilungPresentIfReq, UntersuchungArztBeurteilung> {

    @Override
    @SuppressWarnings({
        "java:S3776" /* acceptable complexity */,
        "java:S1126" /* readability is better with seprate return*/ }
    )
    public boolean isEntityValid(@NonNull UntersuchungArztBeurteilung aerztlicheUntersuchung) {
        AnlassConfig anlassConfig =
            aerztlicheUntersuchung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig();

        if (anlassConfig.getUntersuchungBewegungsapparat().isRequired()
            && aerztlicheUntersuchung.getBewegungsapparat().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinstand().isRequired()
            && aerztlicheUntersuchung.getEinbeinstand() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinhuepfen().isRequired()
            && aerztlicheUntersuchung.getEinbeinhuepfen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungFingeropposition().isRequired()
            && aerztlicheUntersuchung.getFingeropposition().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungMotorik().isRequired()
            && aerztlicheUntersuchung.getMotorik() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungSprache().isRequired()
            && aerztlicheUntersuchung.getSprachfaehigkeit().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungMundZaehne().isRequired()
            && aerztlicheUntersuchung.getMundAndZaehne().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungHaut().isRequired()
            && aerztlicheUntersuchung.getHaut() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungHerz().isRequired()
            && aerztlicheUntersuchung.getHerzauskultation() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungBlutdruck().isRequired()
            && aerztlicheUntersuchung.getBlutdruck() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungVerhalten().isRequired()
            && aerztlicheUntersuchung.getVerhalten() == null) {
            return false;
        }
        return true;
    }
}
