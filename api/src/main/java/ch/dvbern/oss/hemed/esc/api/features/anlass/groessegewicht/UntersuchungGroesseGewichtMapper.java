package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
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
        @NonNull ID<Einladung> einladungId
    ) {
        return wrapToDataJson(einladungId, getByEinladung(einladungId));
    }

    @NonNull
    public UntersuchungGroesseGewichtDataJson saveUntersuchungGroesseGewicht(
        @NonNull ID<Einladung> einladungId,
        @NonNull UntersuchungGroesseGewichtSaveJson saveJson
    ) {
        return wrapToDataJson(einladungId, save(einladungId, saveJson));
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
                groesseGewicht.getGroessePerzentile(),
                groesseGewicht.getGewichtPerzentile(),
                groesseGewicht.getBmiPerzentile(),
                groesseGewicht.getBemerkungen(),
                groesseGewicht.getVersion()
            ),
            groesseGewicht.getAlter(),
            groesseGewicht.getAlterDuodezimal()
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
        groesseGewicht.setGroessePerzentile(saveJson.getGroessePerzentile());
        groesseGewicht.setGewichtPerzentile(saveJson.getGewichtPerzentile());
        groesseGewicht.setBmiPerzentile(saveJson.getBmiPerzentile());
        groesseGewicht.setBemerkungen(saveJson.getBemerkungen());
        return groesseGewicht;
    }

    @Nullable
    private UntersuchungGroesseGewichtJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungGroesseGewicht());
    }

    @NonNull
    private UntersuchungGroesseGewichtJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungGroesseGewichtSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungGroesseGewicht groesseGewicht = untersuchung.getUntersuchungGroesseGewicht();
        UntersuchungGroesseGewicht entity = toEntity(untersuchung, groesseGewicht, saveJson);
        UntersuchungGroesseGewicht persisted = untersuchungGroesseGewichtService.persistWithOptimisticLocking(entity, saveJson.getVersion());
        return toJson(persisted);
    }

    @NonNull
    private List<GroesseGewichtDataPointJson> getPreviousDataPoints(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        List<UntersuchungGroesseGewicht> previousDataPoints = untersuchungGroesseGewichtService.findPreviousByKind(
            untersuchung.getEinladung().getDossier().getKind().getId(),
            untersuchung.getEinladung().getAnlass().getDatum()
        );
        return previousDataPoints.stream()
            .map(GroesseGewichtDataPointMapper::toJson)
            .toList();
    }

    private UntersuchungGroesseGewichtDataJson wrapToDataJson(
        @NonNull ID<Einladung> einladungId,
        @Nullable UntersuchungGroesseGewichtJson untersuchungGroesseGewichtToReturn
    ) {
        Einladung einladung = einladungService.getById(einladungId);
        BigDecimal alter = DateUtil.calculateAlter(
            einladung.getDossier().getKind().getGeburtsdatum(),
            einladung.getAnlass().getDatum());
        String alterDuodezimal = DateUtil.calculateAlterDuodezimal(
            einladung.getDossier().getKind().getGeburtsdatum(),
            einladung.getAnlass().getDatum());
        return new UntersuchungGroesseGewichtDataJson(
            untersuchungGroesseGewichtToReturn,
            getPreviousDataPoints(einladungId),
            alter,
            alterDuodezimal);
    }
}
