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

package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungHoerenUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungHoerenOtoskopieMapper {

    private final AnlassService anlassService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenService;

    @Nullable
    public static UntersuchungHoerenOtoskopieJson toJson(@Nullable UntersuchungHoerenOtoskopie hoeren) {
        if (hoeren == null) {
            return null;
        }
        return new UntersuchungHoerenOtoskopieJson(
            hoeren.getId(),
            EntityInfoJson.fromEntity(hoeren),
            new UntersuchungHoerenOtoskopieSaveJson(
                EnumSetUtil.toList(hoeren.getOtoskopieRechts()),
                hoeren.getOtoskopieRechtsResultatAndere(),
                EnumSetUtil.toList(hoeren.getOtoskopieLinks()),
                hoeren.getOtoskopieLinksResultatAndere(),
                hoeren.getBemerkungen(),
                hoeren.getVersion()
            )
        );
    }

    @Nullable
    public UntersuchungHoerenOtoskopieJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungHoerenOtoskopie());
    }

    @NonNull
    public static UntersuchungHoerenOtoskopie toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungHoerenOtoskopie hoeren,
        @NonNull UntersuchungHoerenOtoskopieSaveJson saveJson
    ) {
        if (hoeren == null) {
            hoeren = UntersuchungHoerenOtoskopie.forUntersuchung(untersuchung);
        }
        hoeren.setOtoskopieRechts(EnumSetUtil.toEnumSet(saveJson.getOtoskopieRechts(), OtoskopieResultat.class));
        hoeren.setOtoskopieRechtsResultatAndere(
            saveJson.getOtoskopieRechts() != null && saveJson.getOtoskopieRechts().contains(OtoskopieResultat.ANDERES)
                ? saveJson.getOtoskopieRechtsResultatAndere()
                : null
        );
        hoeren.setOtoskopieLinks(EnumSetUtil.toEnumSet(saveJson.getOtoskopieLinks(), OtoskopieResultat.class));
        hoeren.setOtoskopieLinksResultatAndere(
            saveJson.getOtoskopieLinks() != null && saveJson.getOtoskopieLinks().contains(OtoskopieResultat.ANDERES)
                ? saveJson.getOtoskopieLinksResultatAndere()
                : null
        );
        hoeren.setBemerkungen(saveJson.getBemerkungen());
        return hoeren;
    }

    @NonNull
    public UntersuchungHoerenOtoskopieJson saveOtoskopie(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungHoerenOtoskopieSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();
        UntersuchungHoerenOtoskopie entity = toEntity(untersuchung, hoerenOtoskopie, saveJson);
        return toJson(untersuchungHoerenService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    @NonNull
    public UntersuchungHoerenOtoskopieSaveJson getDefaultUntersuchungHoerenOtoskopieForAnlassConfig(
        @NonNull ID<Anlass> anlassID
    ) {
        AnlassConfig anlassConfig = anlassService.getById(anlassID).getAnlassConfig();
        OtoskopieResultat defaultOtoskopieResultat = null;
        if (List.of(AnlassTyp.KLASSE_8, AnlassTyp.KLASSE_9, AnlassTyp.KLASSE_4, AnlassTyp.FREMDSPRACHEN)
            .contains(anlassConfig.getAnlassTyp())) {
            defaultOtoskopieResultat = OtoskopieResultat.NICHT_GEPRUEFT;
        }
        return new UntersuchungHoerenOtoskopieSaveJson(
            EnumSetUtil.toList(
                UntersuchungHoerenUtil.getOtoskopieRechts(
                    anlassConfig.getHoerenOtoskopie(),
                    null,
                    defaultOtoskopieResultat)
            ),
            null,
            EnumSetUtil.toList(
                UntersuchungHoerenUtil.getOtoskopieLinks(
                    anlassConfig.getHoerenOtoskopie(),
                    null,
                    defaultOtoskopieResultat)),
            null,
            null,
            0L
        );
    }
}
