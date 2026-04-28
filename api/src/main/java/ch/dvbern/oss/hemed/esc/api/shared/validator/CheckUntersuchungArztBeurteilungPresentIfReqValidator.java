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
            aerztlicheUntersuchung.getUntersuchung().getAnlass().getAnlassConfig();

        if (anlassConfig.getUnterlagenEinverstaendnis().isRequired()
            && aerztlicheUntersuchung.getEinverstaendniserklaerung().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUnterlagenVerhaltensfragebogen().isRequired()
            && aerztlicheUntersuchung.getVerhaltensfragebogen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungBewegungsapparat().isRequired()
            && aerztlicheUntersuchung.getBewegungsapparat().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinstand().isRequired()
            && aerztlicheUntersuchung.getEinbeinstand() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinstandRechtsLinks().isRequired()
            && (aerztlicheUntersuchung.getEinbeinstandRechts() == null
                || aerztlicheUntersuchung.getEinbeinstandLinks() == null)) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinhuepfen().isRequired()
            && aerztlicheUntersuchung.getEinbeinhuepfen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungEinbeinhuepfenRechtsLinks().isRequired()
            && (aerztlicheUntersuchung.getEinbeinhuepfenRechts() == null
                || aerztlicheUntersuchung.getEinbeinhuepfenLinks() == null)) {
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
        if (anlassConfig.getUntersuchungMotorikPunkte().isRequired()
            && aerztlicheUntersuchung.getMotorikPunkte() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungKognition().isRequired()
            && aerztlicheUntersuchung.getKognition() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungKognitionPunkte().isRequired()
            && aerztlicheUntersuchung.getKognitionPunkte() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungSprachfaehigkeitMultiValue().isRequired()
            && aerztlicheUntersuchung.getSprachfaehigkeitMultiValue().isEmpty()) {
            return false;
        }
        if (anlassConfig.getUntersuchungSprachfaehigkeitSingleValue().isRequired()
            && aerztlicheUntersuchung.getSprachfaehigkeitSingleValue() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungSprachePunkte().isRequired()
            && aerztlicheUntersuchung.getSprachfaehigkeitPunkte() == null) {
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
        if (anlassConfig.getUntersuchungHampelmann().isRequired()
            && aerztlicheUntersuchung.getHampelmann() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungBallFangen().isRequired()
            && aerztlicheUntersuchung.getBallFangen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungBallWerfen().isRequired()
            && aerztlicheUntersuchung.getBallWerfen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungNameSchreiben().isRequired()
            && aerztlicheUntersuchung.getNameSchreiben() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungFigurNachmalen().isRequired()
            && aerztlicheUntersuchung.getFigurNachmalen() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungStifthaltung().isRequired()
            && aerztlicheUntersuchung.getStifthaltung() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungAussprache().isRequired()
            && aerztlicheUntersuchung.getAussprache() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungLesefit().isRequired()
            && aerztlicheUntersuchung.getLesefit() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungLesefitPunkte().isRequired()
            && aerztlicheUntersuchung.getLesefitPunkte() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZahlenNachsprechen4().isRequired()
            && aerztlicheUntersuchung.getZahlenNachsprechen4() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZahlenNachsprechen5().isRequired()
            && aerztlicheUntersuchung.getZahlenNachsprechen5() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZeichnenKreis().isRequired()
            && aerztlicheUntersuchung.getZeichnenKreis() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZeichnenViereck().isRequired()
            && aerztlicheUntersuchung.getZeichnenViereck() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZeichnenDreieck().isRequired()
            && aerztlicheUntersuchung.getZeichnenDreieck() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZeichnenMensch().isRequired()
            && aerztlicheUntersuchung.getZeichnenMensch() == null) {
            return false;
        }
        if (anlassConfig.getUntersuchungZeichnenMenschPunkte().isRequired()
            && aerztlicheUntersuchung.getZeichnenMenschPunkte() == null) {
            return false;
        }
        return true;
    }
}
