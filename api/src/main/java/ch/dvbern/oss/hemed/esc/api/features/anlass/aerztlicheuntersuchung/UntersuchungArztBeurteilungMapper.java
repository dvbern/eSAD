/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungArztUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungArztBeurteilungMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungArztBeurteilungService untersuchungArztBeurteilungService;
    private final AnlassService anlassService;

    @Nullable
    public static UntersuchungArztBeurteilungJson toJson(@Nullable UntersuchungArztBeurteilung arzt) {
        if (arzt == null) {
            return null;
        }
        return new UntersuchungArztBeurteilungJson(
            arzt.getId(),
            EntityInfoJson.fromEntity(arzt),
            new UntersuchungArztBeurteilungSaveJson(
                EnumSetUtil.toList(arzt.getEinverstaendniserklaerung()),
                arzt.getVerhaltensfragebogen(),
                arzt.getVerhalten(),
                arzt.getBlutdruck(),
                arzt.getBlutdruckSystolisch(),
                arzt.getBlutdruckDiastolisch(),
                arzt.getHerzauskultation(),
                arzt.getHerzauskultationBemerkung(),
                arzt.getHaut(),
                arzt.getHautBemerkung(),
                EnumSetUtil.toList(arzt.getMundAndZaehne()),
                arzt.getMundAndZaehneBemerkung(),
                arzt.getSprachfaehigkeitSingleValue(),
                EnumSetUtil.toList(arzt.getSprachfaehigkeitMultiValue()),
                arzt.getSprachfaehigkeitBemerkung(),
                arzt.getSprachfaehigkeitPunkte(),
                arzt.getMotorik(),
                arzt.getMotorikPunkte(),
                arzt.getKognition(),
                arzt.getKognitionPunkte(),
                EnumSetUtil.toList(arzt.getBewegungsapparat()),
                arzt.getBewegungsapparatBemerkung(),
                arzt.getEinbeinstand(),
                arzt.getEinbeinstandRechts(),
                arzt.getEinbeinstandLinks(),
                arzt.getEinbeinstandBemerkung(),
                arzt.getEinbeinhuepfen(),
                arzt.getEinbeinhuepfenRechts(),
                arzt.getEinbeinhuepfenLinks(),
                arzt.getEinbeinhuepfenBemerkung(),
                EnumSetUtil.toList(arzt.getFingeropposition()),
                arzt.getFingeroppositionBemerkung(),
                arzt.getHampelmann(),
                arzt.getBallFangen(),
                arzt.getBallWerfen(),
                arzt.getNameSchreiben(),
                arzt.getFigurNachmalen(),
                arzt.getStifthaltung(),
                arzt.getAussprache(),
                arzt.getLesefit(),
                arzt.getLesefitPunkte(),
                arzt.getZahlenNachsprechen4(),
                arzt.getZahlenNachsprechen5(),
                arzt.getZeichnenKreis(),
                arzt.getZeichnenViereck(),
                arzt.getZeichnenDreieck(),
                arzt.getZeichnenMensch(),
                arzt.getZeichnenMenschPunkte(),
                arzt.getVersion()
            )
        );
    }

    @NonNull
    public static UntersuchungArztBeurteilung toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungArztBeurteilung untersuchungArzt,
        @NonNull UntersuchungArztBeurteilungSaveJson saveJson
    ) {
        if (untersuchungArzt == null) {
            untersuchungArzt = UntersuchungArztBeurteilung.forUntersuchung(untersuchung);
        }
        untersuchungArzt.setEinverstaendniserklaerung(EnumSetUtil.toEnumSet(saveJson.getEinverstaendniserklaerung(), Einverstaendniserklaerung.class));
        untersuchungArzt.setVerhaltensfragebogen(saveJson.getVerhaltensfragebogen());
        untersuchungArzt.setVerhalten(saveJson.getVerhalten());
        untersuchungArzt.setBlutdruck(saveJson.getBlutdruck());
        untersuchungArzt.setBlutdruckSystolisch(saveJson.getBlutdruckSystolisch());
        untersuchungArzt.setBlutdruckDiastolisch(saveJson.getBlutdruckDiastolisch());

        untersuchungArzt.setHerzauskultation(saveJson.getHerzauskultation());
        untersuchungArzt.setHerzauskultationBemerkung(
            saveJson.getHerzauskultation() == Herzauskultation.ANDERES ?
                saveJson.getHerzauskultationBemerkung() : null
        );
        untersuchungArzt.setHaut(saveJson.getHaut());
        untersuchungArzt.setHautBemerkung(
            saveJson.getHaut() == Haut.ANDERES ?
                saveJson.getHautBemerkung() : null
        );
        untersuchungArzt.setMundAndZaehne(EnumSetUtil.toEnumSet(saveJson.getMundAndZaehne(), MundAndZaehne.class));
        untersuchungArzt.setMundAndZaehneBemerkung(
            saveJson.getMundAndZaehne() != null && saveJson.getMundAndZaehne().contains(MundAndZaehne.ANDERES) ?
                saveJson.getMundAndZaehneBemerkung() : null
        );
        untersuchungArzt.setSprachfaehigkeitSingleValue(saveJson.getSprachfaehigkeitSingleValue());
        untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSetUtil.toEnumSet(saveJson.getSprachfaehigkeitMultiValue(), Sprachfaehigkeit.class));
        untersuchungArzt.setSprachfaehigkeitBemerkung(
            saveJson.getSprachfaehigkeitMultiValue() != null
                && (saveJson.getSprachfaehigkeitMultiValue().contains(Sprachfaehigkeit.ANDERES) || saveJson.getSprachfaehigkeitMultiValue()
                .contains(Sprachfaehigkeit.ARTIKULATIONSFEHLER)
            ) ?
                saveJson.getSprachfaehigkeitBemerkung() : null
        );
        untersuchungArzt.setSprachfaehigkeitPunkte(saveJson.getSprachfaehigkeitPunkte());
        untersuchungArzt.setMotorik(saveJson.getMotorik());
        untersuchungArzt.setMotorikPunkte(saveJson.getMotorikPunkte());
        untersuchungArzt.setKognition(saveJson.getKognition());
        untersuchungArzt.setKognitionPunkte(saveJson.getKognitionPunkte());
        untersuchungArzt.setBewegungsapparat(EnumSetUtil.toEnumSet(saveJson.getBewegungsapparat(), Bewegungsapparat.class));
        untersuchungArzt.setBewegungsapparatBemerkung(
            saveJson.getBewegungsapparat() != null && saveJson.getBewegungsapparat()
                .contains(Bewegungsapparat.ANDERES) ?
                saveJson.getBewegungsapparatBemerkung() : null
        );

        untersuchungArzt.setEinbeinstand(saveJson.getEinbeinstand());
        untersuchungArzt.setEinbeinstandRechts(saveJson.getEinbeinstandRechts());
        untersuchungArzt.setEinbeinstandLinks(saveJson.getEinbeinstandLinks());
        untersuchungArzt.setEinbeinstandBemerkung(
            saveJson.getEinbeinstand() == Einbeinstand.ANDERES ?
                saveJson.getEinbeinstandBemerkung() : null
        );
        untersuchungArzt.setEinbeinhuepfen(saveJson.getEinbeinhuepfen());
        untersuchungArzt.setEinbeinhuepfenRechts(saveJson.getEinbeinhuepfenRechts());
        untersuchungArzt.setEinbeinhuepfenLinks(saveJson.getEinbeinhuepfenLinks());
        untersuchungArzt.setEinbeinhuepfenBemerkung(
            saveJson.getEinbeinhuepfen() == Einbeinhuepfen.ANDERES ?
                saveJson.getEinbeinhuepfenBemerkung() : null
        );
        untersuchungArzt.setFingeropposition(EnumSetUtil.toEnumSet(saveJson.getFingeropposition(), Fingeropposition.class));

        untersuchungArzt.setFingeroppositionBemerkung(
            saveJson.getFingeropposition() != null && saveJson.getFingeropposition()
                .contains(Fingeropposition.ANDERES) ?
                saveJson.getFingeroppositionBemerkung() : null
        );
        untersuchungArzt.setHampelmann(saveJson.getHampelmann());
        untersuchungArzt.setBallFangen(saveJson.getBallFangen());
        untersuchungArzt.setBallWerfen(saveJson.getBallWerfen());
        untersuchungArzt.setNameSchreiben(saveJson.getNameSchreiben());
        untersuchungArzt.setFigurNachmalen(saveJson.getFigurNachmalen());
        untersuchungArzt.setStifthaltung(saveJson.getStifthaltung());
        untersuchungArzt.setAussprache(saveJson.getAussprache());
        untersuchungArzt.setLesefit(saveJson.getLesefit());
        untersuchungArzt.setLesefitPunkte(saveJson.getLesefitPunkte());
        untersuchungArzt.setZahlenNachsprechen4(saveJson.getZahlenNachsprechen4());
        untersuchungArzt.setZahlenNachsprechen5(saveJson.getZahlenNachsprechen5());
        untersuchungArzt.setZeichnenKreis(saveJson.getZeichnenKreis());
        untersuchungArzt.setZeichnenViereck(saveJson.getZeichnenViereck());
        untersuchungArzt.setZeichnenDreieck(saveJson.getZeichnenDreieck());
        untersuchungArzt.setZeichnenMensch(saveJson.getZeichnenMensch());
        untersuchungArzt.setZeichnenMenschPunkte(saveJson.getZeichnenMenschPunkte());
        return untersuchungArzt;
    }

    @Nullable
    public UntersuchungArztBeurteilungJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungArztBeurteilung());
    }

    @NonNull
    public UntersuchungArztBeurteilungJson save(
        @NonNull ID<Einladung> einladungID, @NonNull
        UntersuchungArztBeurteilungSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungArztBeurteilung arzt = untersuchung.getUntersuchungArztBeurteilung();
        UntersuchungArztBeurteilung entity = toEntity(untersuchung, arzt, saveJson);
        return toJson(untersuchungArztBeurteilungService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    @Nullable
    public UntersuchungArztBeurteilungSaveJson getDefaultUntersuchungArztForAnlassConfig(@NonNull ID<Anlass> anlassID) {
        AnlassConfig config = anlassService.getById(anlassID).getAnlassConfig();
        boolean isKindergarten = config.getAnlassTyp() == AnlassTyp.KINDERGARTEN;
        if (isKindergarten) {
            return new UntersuchungArztBeurteilungSaveJson(
                // Einverstaendniserklaerung
                null,
                // Verhaltensfragebogen
                null,
                // Verhalten
                UntersuchungArztUtil.getVerhalten(config, null, Verhalten.UNAUFFAELLIG),
                // Blutdruck
                null,
                // BlutdruckSystolisch
                null,
                // BlutdruckDiastolisch
                null,
                // Herzauskultation
                UntersuchungArztUtil.getHerzauskultation(config, null, Herzauskultation.UNAUFFAELLIG),
                // HerzauskultationBemerkung
                null,
                // Haut
                UntersuchungArztUtil.getHaut(config, null, Haut.GROBKURSORISCH_UNAUFFAELLIG),
                // HautBemerkung
                null,
                // MundAndZaehne
                EnumSetUtil.toList(UntersuchungArztUtil.getMundAndZaehne(config, null, MundAndZaehne.UNAUFFAELLIG)),
                // MundAndZaehneBemerkung
                null,
                // SprachfaehigkeitSingleValue
                null,
                // SprachfaehigkeitMultiValue
                EnumSetUtil.toList(UntersuchungArztUtil.getSprachfaehigkeit(config, null, Sprachfaehigkeit.GROBKURSORISCH_NORMAL)),
                // SprachfaehigkeitBemerkung
                null,
                // SprachfaehigkeitPunkte
                null,
                // Motorik
                null,
                // MotorikPunkte
                null,
                // Kognition
                null,
                // KognitionPunkte
                null,
                // Bewegungsapparat
                EnumSetUtil.toList(UntersuchungArztUtil.getBewegungsapparat(config, null, Bewegungsapparat.GROBKURSORISCH_NORMAL)),
                // BewegungsapparatBemerkung
                null,
                // Einbeinstand
                UntersuchungArztUtil.getEinbeinstand(config, null, Einbeinstand.SICHER),
                // EinbeinstandRechts
                null,
                // EinbeinstandLinks
                null,
                // EinbeinstandBemerkung
                null,
                // Einbeinhuepfen
                UntersuchungArztUtil.getEinbeinhuepfen(config, null, Einbeinhuepfen.SICHER),
                // EinbeinhuepfenRechts
                null,
                // EinbeinhuepfenLinks
                null,
                // EinbeinhuepfenBemerkung
                null,
                // Fingeropposition
                EnumSetUtil.toList(UntersuchungArztUtil.getFingeropposition(config, null, Fingeropposition.ALTERSENTSPRECHEND)),
                // FingeroppositionBemerkung
                null,
                // Hampelmann
                null,
                // BallFangen
                null,
                // BallWerfen
                null,
                // NameSchreiben
                null,
                // FigurNachmalen
                null,
                // Stifthaltung
                null,
                // Aussprache
                null,
                // Lesefit
                null,
                // LesefitPunkte
                null,
                // ZahlenNachsprechen4
                null,
                // ZahlenNachsprechen5
                null,
                // ZeichnenKreis
                null,
                // ZeichnenViereck
                null,
                // ZeichnenDreieck
                null,
                // ZeichnenMensch
                null,
                // ZeichnenMenschPunkte
                null,
                // Version
                0L
            );
        }
        return null;
    }
}
