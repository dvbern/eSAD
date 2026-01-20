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

package ch.dvbern.oss.hemed.esc.api.features.anlass.sehen;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungSehenUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSehenBeurteilungMapper {

    private final AnlassService anlassService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungSehenBeurteilungService untersuchungSehenBeurteilungService;
    private final TenantProducer tenantProducer;

    @Nullable
    public static UntersuchungSehenBeurteilungJson toJson(
        @Nullable UntersuchungSehenBeurteilung sehen
    ) {
        if (sehen == null) {
            return null;
        }
        return new UntersuchungSehenBeurteilungJson(
            sehen.getId(),
            EntityInfoJson.fromEntity(sehen),
            new UntersuchungSehenBeurteilungSaveJson(
                sehen.getBrilleLinsen(),
                sehen.getStereotest(),
                sehen.getFarbsehen(),
                sehen.getVerwendeteTafel(),
                EnumSetUtil.toList(sehen.getFernvisus()),
                sehen.getFernvisusRechts(),
                sehen.getFernvisusLinks(),
                EnumSetUtil.toList(sehen.getNahvisus()),
                sehen.getNahvisusRechts(),
                sehen.getNahvisusLinks(),
                sehen.getMotilitaet(),
                EnumSetUtil.toList(sehen.getStrabismus()),
                sehen.getStrabismusAuspraegung(),
                sehen.getStrabismusAuftreten(),
                sehen.getInspektion(),
                sehen.getKonvergenz(),
                EnumSetUtil.toList(sehen.getEmpfehlung()),
                sehen.getBemerkungen(),
                sehen.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungSehenBeurteilung toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull UntersuchungSehenBeurteilungSaveJson saveJson
    ) {
        if (sehen == null) {
            sehen = UntersuchungSehenBeurteilung.forUntersuchung(untersuchung);
        }
        sehen.setBrilleLinsen(saveJson.getBrilleLinsen());
        sehen.setStereotest(saveJson.getStereotest());
        sehen.setFarbsehen(saveJson.getFarbsehen());
        sehen.setVerwendeteTafel(saveJson.getVerwendeteTafel());
        sehen.setFernvisus(EnumSetUtil.toEnumSet(saveJson.getFernvisus(), DistanzSehenResultat.class));
        sehen.setFernvisusRechts(saveJson.getFernvisusRechts());
        sehen.setFernvisusLinks(saveJson.getFernvisusLinks());
        sehen.setNahvisus(EnumSetUtil.toEnumSet(saveJson.getNahvisus(), DistanzSehenResultat.class));
        sehen.setNahvisusRechts(saveJson.getNahvisusRechts());
        sehen.setNahvisusLinks(saveJson.getNahvisusLinks());
        sehen.setMotilitaet(saveJson.getMotilitaet());
        sehen.setStrabismus(EnumSetUtil.toEnumSet(saveJson.getStrabismus(), StrabismusResultat.class));
        sehen.setStrabismusAuspraegung(saveJson.getStrabismusAuspraegung());
        sehen.setStrabismusAuftreten(saveJson.getStrabismusAuftreten());
        sehen.setInspektion(saveJson.getInspektion());
        sehen.setKonvergenz(saveJson.getKonvergenz());
        sehen.setEmpfehlung(EnumSetUtil.toEnumSet(saveJson.getEmpfehlung(), SehenEmpfehlung.class));
        sehen.setBemerkungen(saveJson.getBemerkungen());
        return sehen;

    }

    @Nullable
    public UntersuchungSehenBeurteilungJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungSehenBeurteilung());
    }

    @NonNull
    public UntersuchungSehenBeurteilungJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungSehenBeurteilungSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungSehenBeurteilung sehen = untersuchung.getUntersuchungSehenBeurteilung();
        UntersuchungSehenBeurteilung entity = toEntity(untersuchung, sehen, saveJson);
        UntersuchungSehenBeurteilung
            persistedSehen =
            untersuchungSehenBeurteilungService.persistWithOptimisticLocking(entity, saveJson.getVersion());
        return toJson(persistedSehen);
    }

    @NonNull
    public UntersuchungSehenBeurteilungSaveJson getDefaultUntersuchungSehenForAnlassConfig(
        @NonNull ID<Anlass> anlassID
    ) {
        TenantEnum tenant = tenantProducer.produceTenant();

        AnlassConfig config = anlassService.getById(anlassID).getAnlassConfig();
        boolean isKindergarten = config.getAnlassTyp() == AnlassTyp.KINDERGARTEN;
        boolean isFremdsprachen = config.getAnlassTyp() == AnlassTyp.FREMDSPRACHEN;
        BigDecimal fernvisus = isKindergarten ? BigDecimal.valueOf(0.8) : BigDecimal.valueOf(1.0);
        return new UntersuchungSehenBeurteilungSaveJson(
            tenant == TenantEnum.BS ? null: BrilleLinsenResultat.OHNE_KEINE,
            UntersuchungSehenUtil.getStereoTest(
                config,
                null,
                isFremdsprachen ? StereotestResultat.NICHT_GEPRUEFT : StereotestResultat.NORMAL),
            UntersuchungSehenUtil.getFarbsehen(config, null, FarbsehenResultat.NORMAL),
            UntersuchungSehenUtil.getVerwendeteTafel(
                config,
                null,
                isKindergarten ? VerwendetTafelResultat.E_TAFEL : VerwendetTafelResultat.C_TAFEL),
            EnumSetUtil.toList(UntersuchungSehenUtil.getFernvisus(config, null, DistanzSehenResultat.NORMAL)),
            UntersuchungSehenUtil.getFernvisusRechts(config, null, fernvisus),
            UntersuchungSehenUtil.getFernvisusLinks(config, null, fernvisus),
            EnumSetUtil.toList(UntersuchungSehenUtil.getNahvisus(config, null, DistanzSehenResultat.NICHT_GEPRUEFT)),
            null,
            null,
            UntersuchungSehenUtil.getMotilitaet(config, null, MotilitaetResultat.NORMAL),
            EnumSetUtil.toList(UntersuchungSehenUtil.getStrabismus(config, null, StrabismusResultat.KEIN_STRABISMUS)),
            UntersuchungSehenUtil.getStrabismusAuspraegung(config, null, StrabismusAuspraegungResultat.KEIN_STRABISMUS),
            UntersuchungSehenUtil.getStrabismusAuftreten(config, null, StrabismusAuftretenResultat.KEIN_STRABISMUS),
            UntersuchungSehenUtil.getInspektion(config, null, InspektionKonvergenzResultat.UNAUFFAELLIG),
            UntersuchungSehenUtil.getKonvergenz(config, null, InspektionKonvergenzResultat.UNAUFFAELLIG),
            List.of(),
            null,
            0L
        );
    }
}
