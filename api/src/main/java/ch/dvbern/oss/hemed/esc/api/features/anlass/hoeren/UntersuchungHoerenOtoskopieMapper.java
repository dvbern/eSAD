package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfigMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
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
                hoeren.getOtoskopieRechts(),
                hoeren.getOtoskopieRechtsResultatAndere(),
                hoeren.getOtoskopieLinks(),
                hoeren.getOtoskopieLinksResultatAndere(),
                hoeren.getBemerkungen()
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
        hoeren.setOtoskopieRechts(saveJson.getOtoskopieRechts());
        hoeren.setOtoskopieRechtsResultatAndere(
            saveJson.getOtoskopieRechts() != null && saveJson.getOtoskopieRechts().contains(OtoskopieResultat.ANDERES)
                ? saveJson.getOtoskopieRechtsResultatAndere()
                : null
        );
        hoeren.setOtoskopieLinks(saveJson.getOtoskopieLinks());
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
        @NonNull UntersuchungHoerenOtoskopieSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();
        UntersuchungHoerenOtoskopie entity = toEntity(untersuchung, hoerenOtoskopie, saveJson);
        return toJson(untersuchungHoerenService.persist(entity));
    }

    public UntersuchungHoerenOtoskopieSaveJson getDefaultUntersuchungHoerenOtoskopieForAnlassConfig(
        @NonNull ID<Anlass> anlassID) {
        AnlassConfig anlassConfig = anlassService.getById(anlassID).getAnlassConfig();
        return UntersuchungHoerenOtoskopieSaveJson.defaultOtoskopieValuesForAnlassConfig(AnlassConfigMapper.toJson(
            anlassConfig));
    }
}
