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
import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
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
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungSehenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhancedMapper;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
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
        @Nullable UntersuchungSehenBeurteilung sehenBeurteilung
    ) {
        if (sehenBeurteilung == null) {
            return null;
        }
        return new UntersuchungSehenBeurteilungJson(
            sehenBeurteilung.getId(),
            EntityInfoJson.fromEntity(sehenBeurteilung),
            toSaveJson(sehenBeurteilung)
        );
    }

    @NonNull
    public static UntersuchungSehenBeurteilungSaveJson toSaveJson(
        @NonNull UntersuchungSehenBeurteilung sehenBeurteilung) {
        return new UntersuchungSehenBeurteilungSaveJson(
            SingleValueEnhancedMapper.toJson(BrilleLinsenResultat.class, sehenBeurteilung.getBrilleLinsen()),
            SingleValueEnhancedMapper.toJson(StereotestResultat.class, sehenBeurteilung.getStereotest()),
            SingleValueEnhancedMapper.toJson(FarbsehenResultat.class, sehenBeurteilung.getFarbsehen()),
            SingleValueEnhancedMapper.toJson(VerwendetTafelResultat.class, sehenBeurteilung.getVerwendeteTafel()),
            MultiValueEnhancedMapper.toJson(DistanzSehenResultat.class, sehenBeurteilung.getFernvisus()),
            sehenBeurteilung.getFernvisusRechts(),
            sehenBeurteilung.getFernvisusLinks(),
            SingleValueEnhancedMapper.toJson(
                DistanzSehenAugeEinzelnResultat.class,
                sehenBeurteilung.getFernvisusAugeLinks()),
            SingleValueEnhancedMapper.toJson(
                DistanzSehenAugeEinzelnResultat.class,
                sehenBeurteilung.getFernvisusAugeRechts()),
            MultiValueEnhancedMapper.toJson(DistanzSehenResultat.class, sehenBeurteilung.getNahvisus()),
            sehenBeurteilung.getNahvisusRechts(),
            sehenBeurteilung.getNahvisusLinks(),
            SingleValueEnhancedMapper.toJson(MotilitaetResultat.class, sehenBeurteilung.getMotilitaet()),
            MultiValueEnhancedMapper.toJson(StrabismusResultat.class, sehenBeurteilung.getStrabismus()),
            SingleValueEnhancedMapper.toJson(
                StrabismusAuspraegungResultat.class,
                sehenBeurteilung.getStrabismusAuspraegung()),
            SingleValueEnhancedMapper.toJson(
                StrabismusAuftretenResultat.class,
                sehenBeurteilung.getStrabismusAuftreten()),
            SingleValueEnhancedMapper.toJson(InspektionKonvergenzResultat.class, sehenBeurteilung.getInspektion()),
            SingleValueEnhancedMapper.toJson(InspektionKonvergenzResultat.class, sehenBeurteilung.getKonvergenz()),
            MultiValueEnhancedMapper.toJson(SehenEmpfehlung.class, sehenBeurteilung.getEmpfehlung()),
            sehenBeurteilung.getBemerkungen(),
            sehenBeurteilung.getVersion()
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
        sehen.setBrilleLinsen(SingleValueEnhancedMapper.toEntity(
            BrilleLinsenResultat.class,
            saveJson.getBrilleLinsen()));
        sehen.setStereotest(SingleValueEnhancedMapper.toEntity(StereotestResultat.class, saveJson.getStereotest()));
        sehen.setFarbsehen(SingleValueEnhancedMapper.toEntity(FarbsehenResultat.class, saveJson.getFarbsehen()));
        sehen.setVerwendeteTafel(SingleValueEnhancedMapper.toEntity(
            VerwendetTafelResultat.class,
            saveJson.getVerwendeteTafel()));
        sehen.setFernvisus(MultiValueEnhancedMapper.toEntity(DistanzSehenResultat.class, saveJson.getFernvisus()));
        sehen.setFernvisusRechts(saveJson.getFernvisusRechts());
        sehen.setFernvisusLinks(saveJson.getFernvisusLinks());
        sehen.setNahvisus(MultiValueEnhancedMapper.toEntity(DistanzSehenResultat.class, saveJson.getNahvisus()));
        sehen.setNahvisusRechts(saveJson.getNahvisusRechts());
        sehen.setNahvisusLinks(saveJson.getNahvisusLinks());
        sehen.setMotilitaet(SingleValueEnhancedMapper.toEntity(MotilitaetResultat.class, saveJson.getMotilitaet()));
        sehen.setStrabismus(MultiValueEnhancedMapper.toEntity(StrabismusResultat.class, saveJson.getStrabismus()));
        sehen.setStrabismusAuspraegung(SingleValueEnhancedMapper.toEntity(
            StrabismusAuspraegungResultat.class,
            saveJson.getStrabismusAuspraegung()));
        sehen.setStrabismusAuftreten(SingleValueEnhancedMapper.toEntity(
            StrabismusAuftretenResultat.class,
            saveJson.getStrabismusAuftreten()));
        sehen.setInspektion(SingleValueEnhancedMapper.toEntity(
            InspektionKonvergenzResultat.class,
            saveJson.getInspektion()));
        sehen.setKonvergenz(SingleValueEnhancedMapper.toEntity(
            InspektionKonvergenzResultat.class,
            saveJson.getKonvergenz()));
        sehen.setEmpfehlung(MultiValueEnhancedMapper.toEntity(SehenEmpfehlung.class, saveJson.getEmpfehlung()));
        sehen.setBemerkungen(saveJson.getBemerkungen());
        sehen.setFernvisusAugeLinks(SingleValueEnhancedMapper.toEntity(
            DistanzSehenAugeEinzelnResultat.class,
            saveJson.getFernvisusAugeLinks()));
        sehen.setFernvisusAugeRechts(SingleValueEnhancedMapper.toEntity(
            DistanzSehenAugeEinzelnResultat.class,
            saveJson.getFernvisusAugeRechts()));
        return sehen;

    }

    @Nullable
    public UntersuchungSehenBeurteilungJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungSehenBeurteilung());
    }

    @NonNull
    public UntersuchungSehenBeurteilungJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungSehenBeurteilungSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
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
            SingleValueEnhancedMapper.toJson(
                BrilleLinsenResultat.class,
                UntersuchungSehenUtil.getBrilleLinsenDefault(
                    config,
                    tenant == TenantEnum.BS ? null : BrilleLinsenResultat.OHNE_KEINE)
            ),
            SingleValueEnhancedMapper.toJson(
                StereotestResultat.class,
                UntersuchungSehenUtil.getStereoTestDefault(
                    config,
                    isFremdsprachen ? StereotestResultat.NICHT_GEPRUEFT : StereotestResultat.NORMAL)
            ),
            SingleValueEnhancedMapper.toJson(
                FarbsehenResultat.class,
                UntersuchungSehenUtil.getFarbsehenDefault(config, FarbsehenResultat.NORMAL)
            ),
            SingleValueEnhancedMapper.toJson(
                VerwendetTafelResultat.class,
                UntersuchungSehenUtil.getVerwendeteTafelDefault(
                    config,
                    isKindergarten ? VerwendetTafelResultat.E_TAFEL : VerwendetTafelResultat.C_TAFEL)
            ),
            MultiValueEnhancedMapper.toJson(
                DistanzSehenResultat.class,
                UntersuchungSehenUtil.getFernvisusDefault(
                    config, DistanzSehenResultat.NORMAL)),
            UntersuchungSehenUtil.getFernvisusRechts(config, null, fernvisus),
            UntersuchungSehenUtil.getFernvisusLinks(config, null, fernvisus),
            null,
            null,
            MultiValueEnhancedMapper.toJson(
                DistanzSehenResultat.class,
                UntersuchungSehenUtil.getNahvisusDefault(
                    config, DistanzSehenResultat.NICHT_GEPRUEFT)),
            null,
            null,
            SingleValueEnhancedMapper.toJson(
                MotilitaetResultat.class,
                UntersuchungSehenUtil.getMotilitaetDefault(config, MotilitaetResultat.NORMAL)
            ),
            MultiValueEnhancedMapper.toJson(
                StrabismusResultat.class,
                UntersuchungSehenUtil.getStrabismusDefault(
                    config, StrabismusResultat.KEIN_STRABISMUS)),

            SingleValueEnhancedMapper.toJson(
                StrabismusAuspraegungResultat.class,
                UntersuchungSehenUtil.getStrabismusAuspraegung(config, StrabismusAuspraegungResultat.KEIN_STRABISMUS)
            ),
            SingleValueEnhancedMapper.toJson(
                StrabismusAuftretenResultat.class,
                UntersuchungSehenUtil.getStrabismusAuftreten(config, StrabismusAuftretenResultat.KEIN_STRABISMUS)
            ),
            SingleValueEnhancedMapper.toJson(
                InspektionKonvergenzResultat.class,
                UntersuchungSehenUtil.getInspektion(config, InspektionKonvergenzResultat.UNAUFFAELLIG)
            ),
            SingleValueEnhancedMapper.toJson(
                InspektionKonvergenzResultat.class,
                UntersuchungSehenUtil.getKonvergenz(config, InspektionKonvergenzResultat.UNAUFFAELLIG)
            ),
            MultiValueEnhancedMapper.toJson(
                SehenEmpfehlung.class,
                MultiValueEnhanced.of(EnumSet.noneOf(SehenEmpfehlung.class), null)
            ),
            null,
            0L
        );
    }
}
