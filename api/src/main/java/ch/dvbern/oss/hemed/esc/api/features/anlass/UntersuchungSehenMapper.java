package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungSehenUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSehenMapper {

    private final AnlassService anlassService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungSehenService untersuchungSehenService;
    private final UntersuchungMassnahmenMapper untersuchungMassnahmenMapper;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;

    @NonNull
    public static UntersuchungSehenJson toJson(
        @NonNull UntersuchungSehen sehen,
        @Nullable UntersuchungMassnahmen massnahmen
    ) {
        return new UntersuchungSehenJson(
            sehen.getId(),
            EntityInfoJson.fromEntity(sehen),
            new UntersuchungSehenSaveJson(
                sehen.getBrilleLinsen(),
                sehen.getStereotest(),
                sehen.getFarbsehen(),
                sehen.getVerwendeteTafel(),
                sehen.getFernvisus(),
                sehen.getFernvisusRechts(),
                sehen.getFernvisusLinks(),
                sehen.getNahvisus(),
                sehen.getNahvisusRechts(),
                sehen.getNahvisusLinks(),
                sehen.getMotilitaet(),
                sehen.getStrabismus(),
                sehen.getStrabismusAuspraegung(),
                sehen.getStrabismusAuftreten(),
                sehen.getInspektion(),
                sehen.getKonvergenz(),
                sehen.getBemerkungen(),
                UntersuchungMassnahmenMapper.toSehenMassnahmenJson(massnahmen)
            )
        );
    }

    @NonNull
    private UntersuchungSehen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungSehen sehen,
        @NonNull UntersuchungSehenSaveJson saveJson
    ) {
        if (sehen == null) {
            sehen = UntersuchungSehen.forUntersuchung(untersuchung);
        }
        sehen.setBrilleLinsen(saveJson.getBrilleLinsen());
        sehen.setStereotest(saveJson.getStereotest());
        sehen.setFarbsehen(saveJson.getFarbsehen());
        sehen.setVerwendeteTafel(saveJson.getVerwendeteTafel());
        sehen.setFernvisus(saveJson.getFernvisus());
        sehen.setFernvisusRechts(saveJson.getFernvisusRechts());
        sehen.setFernvisusLinks(saveJson.getFernvisusLinks());
        sehen.setNahvisus(saveJson.getNahvisus());
        sehen.setNahvisusRechts(saveJson.getNahvisusRechts());
        sehen.setNahvisusLinks(saveJson.getNahvisusLinks());
        sehen.setMotilitaet(saveJson.getMotilitaet());
        sehen.setStrabismus(saveJson.getStrabismus());
        sehen.setStrabismusAuspraegung(saveJson.getStrabismusAuspraegung());
        sehen.setStrabismusAuftreten(saveJson.getStrabismusAuftreten());
        sehen.setInspektion(saveJson.getInspektion());
        sehen.setKonvergenz(saveJson.getKonvergenz());
        sehen.setBemerkungen(saveJson.getBemerkungen());
        return sehen;

    }

    @Nullable
    public UntersuchungSehenJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        if (untersuchung.getUntersuchungSehen() != null) {
            return toJson(untersuchung.getUntersuchungSehen(), untersuchung.getUntersuchungMassnahmen());
        }
        return null;
    }

    @NonNull
    public UntersuchungSehenJson save(@NonNull ID<Einladung> einladungID, @NonNull UntersuchungSehenSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungSehen sehen = untersuchung.getUntersuchungSehen();
        UntersuchungSehen entity = toEntity(untersuchung, sehen, saveJson);
        UntersuchungSehen persistedSehen = untersuchungSehenService.persist(entity);

        UntersuchungMassnahmen massnahmen = null;
        if (saveJson.getMassnahmen() != null) {
            massnahmen = untersuchungMassnahmenService.persist(untersuchungMassnahmenMapper.toSehenMassnahmenEntity(
                untersuchung,
                untersuchung.getUntersuchungMassnahmen(),
                saveJson.getMassnahmen()));
        }
        return toJson(persistedSehen, massnahmen);
    }

    @NonNull
    public UntersuchungSehenSaveJson getDefaultUntersuchungSehenForAnlassConfig(@NonNull ID<Anlass> anlassID) {
        AnlassConfig config = anlassService.getById(anlassID).getAnlassConfig();
        boolean isKindergarten = config.getAnlassTyp() == AnlassTyp.KINDERGARTEN;
        boolean isFremdsprachen = config.getAnlassTyp() == AnlassTyp.FREMDSPRACHEN;
        BigDecimal fernvisus = isKindergarten ? BigDecimal.valueOf(0.8) : BigDecimal.valueOf(1.0);
        return new UntersuchungSehenSaveJson(
            null,
            UntersuchungSehenUtil.getStereoTest(
                config,
                null,
                isFremdsprachen ? StereotestResultat.NICHT_GEPRUEFT : StereotestResultat.NORMAL),
            UntersuchungSehenUtil.getFarbsehen(config, null, FarbsehenResultat.NORMAL),
            UntersuchungSehenUtil.getVerwendeteTafel(
                config,
                null,
                isKindergarten ? VerwendetTafelResultat.E_TAFEL : VerwendetTafelResultat.C_TAFEL),
            UntersuchungSehenUtil.getFernvisus(config, null, DistanzSehenResultat.NORMAL),
            UntersuchungSehenUtil.getFernvisusRechts(config, null, fernvisus),
            UntersuchungSehenUtil.getFernvisusLinks(config, null, fernvisus),
            UntersuchungSehenUtil.getNahvisus(config, null, DistanzSehenResultat.NICHT_GEPRUEFT),
            null,
            null,
            UntersuchungSehenUtil.getMotilitaet(config, null, MotilitaetResultat.NORMAL),
            UntersuchungSehenUtil.getStrabismus(config, null, StrabismusResultat.KEIN_STRABISMUS),
            UntersuchungSehenUtil.getStrabismusAuspraegung(config, null, StrabismusAuspraegungResultat.KEIN_STRABISMUS),
            UntersuchungSehenUtil.getStrabismusAuftreten(config, null, StrabismusAuftretenResultat.KEIN_STRABISMUS),
            UntersuchungSehenUtil.getInspektion(config, null, InspektionKonvergenzResultat.UNAUFFAELLIG),
            UntersuchungSehenUtil.getKonvergenz(config, null, InspektionKonvergenzResultat.UNAUFFAELLIG),
            null,
            null
        );
    }
}
