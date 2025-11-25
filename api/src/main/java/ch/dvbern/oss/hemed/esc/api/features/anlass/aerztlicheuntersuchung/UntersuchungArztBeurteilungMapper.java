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
                EnumSetUtil.toList(arzt.getSprachfaehigkeit()),
                arzt.getSprachfaehigkeitBemerkung(),
                arzt.getMotorik(),
                EnumSetUtil.toList(arzt.getBewegungsapparat()),
                arzt.getBewegungsapparatBemerkung(),
                arzt.getEinbeinstand(),
                arzt.getEinbeinstandBemerkung(),
                arzt.getEinbeinhuepfen(),
                arzt.getEinbeinhuepfenBemerkung(),
                EnumSetUtil.toList(arzt.getFingeropposition()),
                arzt.getFingeroppositionBemerkung(),
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

        untersuchungArzt.setSprachfaehigkeit(EnumSetUtil.toEnumSet(saveJson.getSprachfaehigkeit(), Sprachfaehigkeit.class));
        untersuchungArzt.setSprachfaehigkeitBemerkung(
            saveJson.getSprachfaehigkeit() != null
                && (saveJson.getSprachfaehigkeit().contains(Sprachfaehigkeit.ANDERES) || saveJson.getSprachfaehigkeit()
                .contains(Sprachfaehigkeit.ARTIKULATIONSFEHLER)
            ) ?
                saveJson.getSprachfaehigkeitBemerkung() : null
        );
        untersuchungArzt.setMotorik(saveJson.getMotorik());
        untersuchungArzt.setBewegungsapparat(EnumSetUtil.toEnumSet(saveJson.getBewegungsapparat(), Bewegungsapparat.class));
        untersuchungArzt.setBewegungsapparatBemerkung(
            saveJson.getBewegungsapparat() != null && saveJson.getBewegungsapparat()
                .contains(Bewegungsapparat.ANDERES) ?
                saveJson.getBewegungsapparatBemerkung() : null
        );

        untersuchungArzt.setEinbeinstand(saveJson.getEinbeinstand());
        untersuchungArzt.setEinbeinstandBemerkung(
            saveJson.getEinbeinstand() == Einbeinstand.ANDERES ?
                saveJson.getEinbeinstandBemerkung() : null
        );
        untersuchungArzt.setEinbeinhuepfen(saveJson.getEinbeinhuepfen());
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
                UntersuchungArztUtil.getVerhalten(config, null, Verhalten.UNAUFFAELLIG),
                // Verhalten
                null,
                // Blutdruck
                null,
                // BlutdruckSystolisch
                null,
                // BlutdruckDiastolisch
                UntersuchungArztUtil.getHerzauskultation(config, null, Herzauskultation.UNAUFFAELLIG),
                // Herzauskultation
                null,
                // HerzauskultationBemerkung
                UntersuchungArztUtil.getHaut(config, null, Haut.GROBKURSORISCH_UNAUFFAELLIG),
                // Haut
                null,
                // HautBemerkung
                EnumSetUtil.toList(UntersuchungArztUtil.getMundAndZaehne(config, null, MundAndZaehne.UNAUFFAELLIG)),
                // MundAndZaehne
                null,
                // MundAndZaehneBemerkung
                EnumSetUtil.toList(UntersuchungArztUtil.getSprachfaehigkeit(config, null, Sprachfaehigkeit.GROBKURSORISCH_NORMAL)),
                // Sprachfaehigkeit
                null,
                // SprachfaehigkeitBemerkung
                null,
                // Motorik
                EnumSetUtil.toList(UntersuchungArztUtil.getBewegungsapparat(config, null, Bewegungsapparat.GROBKURSORISCH_NORMAL)),
                // Bewegungsapparat
                null,
                // BewegungsapparatBemerkung
                UntersuchungArztUtil.getEinbeinstand(config, null, Einbeinstand.SICHER),
                // Einbeinstand
                null,
                // EinbeinstandBemerkung
                UntersuchungArztUtil.getEinbeinhuepfen(config, null, Einbeinhuepfen.SICHER),
                // Einbeinhuepfen
                null,
                // EinbeinhuepfenBemerkung
                EnumSetUtil.toList(UntersuchungArztUtil.getFingeropposition(config, null, Fingeropposition.ALTERSENTSPRECHEND)),
                // Fingeropposition
                null,
                // FingeroppositionBemerkung
                0L
                // Version
            );
        }
        return null;
    }
}
