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
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungHoerenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhancedMapper;
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
            toSaveJson(hoeren)
        );
    }

    @NonNull
    public static UntersuchungHoerenOtoskopieSaveJson toSaveJson(@NonNull UntersuchungHoerenOtoskopie hoeren) {
        return new UntersuchungHoerenOtoskopieSaveJson(
            MultiValueEnhancedMapper.toJson(OtoskopieResultat.class, hoeren.getOtoskopieRechts()),
            hoeren.getOtoskopieRechtsResultatAndere(),
            MultiValueEnhancedMapper.toJson(OtoskopieResultat.class, hoeren.getOtoskopieLinks()),
            hoeren.getOtoskopieLinksResultatAndere(),
            hoeren.getBemerkungen(),
            hoeren.getVersion()
        );
    }

    @Nullable
    public UntersuchungHoerenOtoskopieJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
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
        hoeren.setOtoskopieRechts(MultiValueEnhancedMapper.toEntity(
            OtoskopieResultat.class,
            saveJson.getOtoskopieRechts()));
        hoeren.setOtoskopieRechtsResultatAndere(
            ValueEnhancedUtil.getEnumSetOrEmpty(
                MultiValueEnhancedMapper.toEntity(OtoskopieResultat.class, saveJson.getOtoskopieRechts()),
                OtoskopieResultat.class).contains(OtoskopieResultat.ANDERES)
                ? saveJson.getOtoskopieRechtsResultatAndere()
                : null
        );
        hoeren.setOtoskopieLinks(MultiValueEnhancedMapper.toEntity(
            OtoskopieResultat.class,
            saveJson.getOtoskopieLinks()));
        hoeren.setOtoskopieLinksResultatAndere(
            ValueEnhancedUtil.getEnumSetOrEmpty(
                MultiValueEnhancedMapper.toEntity(OtoskopieResultat.class, saveJson.getOtoskopieLinks()),
                OtoskopieResultat.class).contains(OtoskopieResultat.ANDERES)
                ? saveJson.getOtoskopieLinksResultatAndere()
                : null
        );
        hoeren.setBemerkungen(saveJson.getBemerkungen());
        return hoeren;
    }

    @NonNull
    public UntersuchungHoerenOtoskopieJson saveOtoskopie(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungHoerenOtoskopieSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
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

        // Warning: it is important for the dirty check to send null values if there is no default value for otoskopie
        return new UntersuchungHoerenOtoskopieSaveJson(
            MultiValueEnhancedMapper.toJson(
                OtoskopieResultat.class,
                UntersuchungHoerenUtil.getOtoskopieDefault(anlassConfig.getHoerenOtoskopie(), defaultOtoskopieResultat)
            ),
            null,
            MultiValueEnhancedMapper.toJson(
                OtoskopieResultat.class,
                UntersuchungHoerenUtil.getOtoskopieDefault(anlassConfig.getHoerenOtoskopie(), defaultOtoskopieResultat)
            ),
            null,
            null,
            0L
        );
    }
}
