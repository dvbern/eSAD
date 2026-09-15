package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.GroesseGewichtDataPointJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.UntersuchungGroesseGewichtDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.UntersuchungGroesseGewichtJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.UntersuchungGroesseGewichtSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhancedMapper;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungGroesseGewichtMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final EinladungService einladungService;

    @NonNull
    public UntersuchungGroesseGewichtDataJson getUntersuchungGroesseGewicht(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        return wrapToDataJson(wrappedId, getByEinladung(wrappedId, hasAuthorizationUntersuchung));
    }

    @NonNull
    public UntersuchungGroesseGewichtDataJson saveUntersuchungGroesseGewicht(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungGroesseGewichtSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        return wrapToDataJson(wrappedId, save(wrappedId, saveJson, hasAuthorizationUntersuchung));
    }

    @Nullable
    public static UntersuchungGroesseGewichtJson toJson(
        @Nullable UntersuchungGroesseGewicht groesseGewicht
    ) {
        if (groesseGewicht == null) {
            return null;
        }
        return new UntersuchungGroesseGewichtJson(
            groesseGewicht.getId(),
            EntityInfoJson.fromEntity(groesseGewicht),
            new UntersuchungGroesseGewichtSaveJson(
                groesseGewicht.getGroesse(),
                groesseGewicht.getGewicht(),
                SingleValueEnhancedMapper.toJson(
                    PerzentileGroesse.class,
                    groesseGewicht.getGroessePerzentileSingleValue()),
                MultiValueEnhancedMapper.toJson(
                    PerzentileGroesse.class,
                    groesseGewicht.getGroessePerzentileMultiValue()),
                SingleValueEnhancedMapper.toJson(
                    PerzentileGewicht.class,
                    groesseGewicht.getGewichtPerzentileSingleValue()),
                MultiValueEnhancedMapper.toJson(
                    PerzentileGewicht.class,
                    groesseGewicht.getGewichtPerzentileMultiValue()),
                SingleValueEnhancedMapper.toJson(
                    PerzentileBmi.class,
                    groesseGewicht.getBmiPerzentileSingleValue()),
                MultiValueEnhancedMapper.toJson(
                    PerzentileBmi.class,
                    groesseGewicht.getBmiPerzentileMultiValue()),
                groesseGewicht.getBemerkungen(),
                groesseGewicht.getVersion()
            ),
            groesseGewicht.getUntersuchung().isAnonymerEintrag() ? null : groesseGewicht.getAlter(),
            groesseGewicht.getUntersuchung().isAnonymerEintrag() ? null : groesseGewicht.getAlterDuodezimal()
        );
    }

    @NonNull
    private UntersuchungGroesseGewicht toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull UntersuchungGroesseGewichtSaveJson saveJson
    ) {
        if (groesseGewicht == null) {
            groesseGewicht = UntersuchungGroesseGewicht.forUntersuchung(untersuchung);
        }
        groesseGewicht.setGroesse(saveJson.getGroesse());
        groesseGewicht.setGewicht(saveJson.getGewicht());
        groesseGewicht.setBmi(GroesseGewichtUtil.calculateBmi(
            groesseGewicht.getGroesse(),
            groesseGewicht.getGewicht()));
        groesseGewicht.setGroessePerzentileSingleValue(SingleValueEnhancedMapper.toEntity(
            PerzentileGroesse.class,
            saveJson.getGroessePerzentileSingleValue()));
        groesseGewicht.setGroessePerzentileMultiValue(MultiValueEnhancedMapper.toEntity(
            PerzentileGroesse.class,
            saveJson.getGroessePerzentileMultiValue()));
        groesseGewicht.setGewichtPerzentileSingleValue(SingleValueEnhancedMapper.toEntity(
            PerzentileGewicht.class,
            saveJson.getGewichtPerzentileSingleValue()));
        groesseGewicht.setGewichtPerzentileMultiValue(MultiValueEnhancedMapper.toEntity(
            PerzentileGewicht.class,
            saveJson.getGewichtPerzentileMultiValue()));
        groesseGewicht.setBmiPerzentileSingleValue(SingleValueEnhancedMapper.toEntity(
            PerzentileBmi.class,
            saveJson.getBmiPerzentileSingleValue()));
        groesseGewicht.setBmiPerzentileMultiValue(MultiValueEnhancedMapper.toEntity(
            PerzentileBmi.class,
            saveJson.getBmiPerzentileMultiValue()));
        groesseGewicht.setBemerkungen(saveJson.getBemerkungen());
        return groesseGewicht;
    }

    @Nullable
    private UntersuchungGroesseGewichtJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungGroesseGewicht());
    }

    @NonNull
    private UntersuchungGroesseGewichtJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungGroesseGewichtSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungGroesseGewicht groesseGewicht = untersuchung.getUntersuchungGroesseGewicht();
        UntersuchungGroesseGewicht entity = toEntity(untersuchung, groesseGewicht, saveJson);
        UntersuchungGroesseGewicht persisted =
            untersuchungGroesseGewichtService.persistWithOptimisticLocking(entity, saveJson.getVersion());
        return toJson(persisted);
    }

    @NonNull
    private List<GroesseGewichtDataPointJson> getPreviousDataPoints(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        List<UntersuchungGroesseGewicht> previousDataPoints = untersuchungGroesseGewichtService.findPreviousByKind(
            untersuchung.getEinladungRequired().getDossier().getKind().getId(),
            untersuchung.getAnlass().getDatum()
        );
        return previousDataPoints.stream()
            .map(GroesseGewichtDataPointMapper::toJson)
            .toList();
    }

    private UntersuchungGroesseGewichtDataJson wrapToDataJson(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @Nullable UntersuchungGroesseGewichtJson untersuchungGroesseGewichtToReturn
    ) {
        if (wrappedId.isAnonymerEintrag()) {
            return new UntersuchungGroesseGewichtDataJson(
                untersuchungGroesseGewichtToReturn,
                List.of(),
                null,
                null);
        }
        Einladung einladung = einladungService.getById(wrappedId.toEinladungId());
        BigDecimal alter = DateUtil.calculateAlter(
            einladung.getDossier().getKind().getGeburtsdatum(),
            einladung.getAnlass().getDatum());
        String alterDuodezimal = DateUtil.calculateAlterDuodezimal(
            einladung.getDossier().getKind().getGeburtsdatum(),
            einladung.getAnlass().getDatum());
        return new UntersuchungGroesseGewichtDataJson(
            untersuchungGroesseGewichtToReturn,
            getPreviousDataPoints(wrappedId.toEinladungId()),
            alter,
            alterDuodezimal);
    }
}
