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

import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.UntersuchungHoerenDefaultValueUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungHoerenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhancedMapper;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungHoerenAudiogrammMapper {

    private final AnlassService anlassService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenService;

    @Nullable
    public static UntersuchungHoerenAudiogrammJson toJson(@Nullable UntersuchungHoerenAudiogramm hoeren) {
        if (hoeren == null) {
            return null;
        }
        return new UntersuchungHoerenAudiogrammJson(
            hoeren.getId(),
            EntityInfoJson.fromEntity(hoeren),
            toSaveJson(hoeren)
        );
    }

    @NonNull
    public static UntersuchungHoerenAudiogrammSaveJson toSaveJson(@NonNull UntersuchungHoerenAudiogramm hoeren) {
        return new UntersuchungHoerenAudiogrammSaveJson(
            AudiogrammMapper.toJson(hoeren.getAudiogrammRechts()),
            AudiogrammMapper.toJson(hoeren.getAudiogrammLinks()),
            MultiValueEnhancedMapper.toJson(AudiogrammBeurteilung.class, hoeren.getAudiogrammBeurteilung()),
            MultiValueEnhancedMapper.toJson(HoerenZusaetzlicheAngaben.class, hoeren.getZusaetzlicheAngaben()),
            MultiValueEnhancedMapper.toJson(HoerenEmpfehlung.class, hoeren.getEmpfehlung()),
            hoeren.getBemerkungen(),
            SingleValueEnhancedMapper
                .toJson(AudiogrammSimpel.class, hoeren.getAudiogrammSimpel()),
            SingleValueEnhancedMapper
                .toJson(HoerenHoerhilfe.class, hoeren.getHoerhilfe()),
            hoeren.getVersion()
        );
    }

    @Nullable
    public UntersuchungHoerenAudiogrammJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungHoerenAudiogramm());
    }

    @NonNull
    public static UntersuchungHoerenAudiogramm toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungHoerenAudiogramm hoeren,
        @NonNull UntersuchungHoerenAudiogrammSaveJson saveJson
    ) {
        if (hoeren == null) {
            hoeren = UntersuchungHoerenAudiogramm.forUntersuchung(untersuchung);
        }
        hoeren.setAudiogrammRechts(AudiogrammMapper.toEntity(
            hoeren.getAudiogrammRechts(),
            saveJson.getAudiogrammRechts()));
        hoeren.setAudiogrammLinks(AudiogrammMapper.toEntity(
            hoeren.getAudiogrammLinks(),
            saveJson.getAudiogrammLinks()));
        hoeren.setAudiogrammBeurteilung(MultiValueEnhancedMapper.toEntity(
            AudiogrammBeurteilung.class,
            saveJson.getAudiogrammBeurteilung()));
        hoeren.setZusaetzlicheAngaben(MultiValueEnhancedMapper.toEntity(
            HoerenZusaetzlicheAngaben.class,
            saveJson.getZusaetzlicheAngaben()));
        hoeren.setEmpfehlung(MultiValueEnhancedMapper.toEntity(HoerenEmpfehlung.class, saveJson.getEmpfehlung()));
        hoeren.setBemerkungen(saveJson.getBemerkungen());
        hoeren.setAudiogrammSimpel(SingleValueEnhancedMapper.toEntity(
            AudiogrammSimpel.class,
            saveJson.getAudiogrammSimpel()));
        hoeren.setHoerhilfe(SingleValueEnhancedMapper.toEntity(HoerenHoerhilfe.class, saveJson.getHoerhilfe()));
        return hoeren;
    }

    @NonNull
    public UntersuchungHoerenAudiogrammJson saveAudiogramm(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungHoerenAudiogrammSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();
        UntersuchungHoerenAudiogramm entity = toEntity(untersuchung, hoerenAudiogramm, saveJson);
        if (entity.getAudiogrammRechts() != null && saveJson.getAudiogrammRechts() != null) {
            untersuchungHoerenService.cascadeOptimisticLocking(
                entity.getAudiogrammRechts(),
                saveJson.getAudiogrammRechts().getVersion());
        }
        if (entity.getAudiogrammLinks() != null && saveJson.getAudiogrammLinks() != null) {
            untersuchungHoerenService.cascadeOptimisticLocking(
                entity.getAudiogrammLinks(),
                saveJson.getAudiogrammLinks().getVersion());
        }
        return toJson(untersuchungHoerenService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }

    @NonNull
    public UntersuchungHoerenAudiogrammSaveJson getDefaultUntersuchungHoerenAudiogrammForAnlassConfig(
        @NonNull ID<Anlass> anlassID
    ) {
        AnlassConfig anlassConfig = anlassService.getById(anlassID).getAnlassConfig();
        return new UntersuchungHoerenAudiogrammSaveJson(

            UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(anlassConfig),
            UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(anlassConfig),
            MultiValueEnhancedMapper.toJson(
                AudiogrammBeurteilung.class,
                UntersuchungHoerenUtil.getAudiogrammBeurteilungDefault(
                    anlassConfig.getHoerenAudiogrammBeurteilung(),
                    AudiogrammBeurteilung.NORMAL)),
            MultiValueEnhancedMapper.toJson(
                HoerenZusaetzlicheAngaben.class,
                UntersuchungHoerenUtil.getZusaetzlicheAngabenDefault(
                    anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben(), HoerenZusaetzlicheAngaben.KEINE)),
            MultiValueEnhancedMapper.toJson(
                HoerenEmpfehlung.class,
                MultiValueEnhanced.of(EnumSet.noneOf(HoerenEmpfehlung.class), null)
            ),
            null,
            null,
            SingleValueEnhancedMapper.toJson(
                HoerenHoerhilfe.class,
                UntersuchungHoerenUtil.getHoerenHoerhilfeDefault(anlassConfig, HoerenHoerhilfe.NEIN)
            ),
            0L
        );
    }
}
