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

            new UntersuchungHoerenAudiogrammSaveJson(
                AudiogrammMapper.toJson(hoeren.getAudiogrammRechts()),
                AudiogrammMapper.toJson(hoeren.getAudiogrammLinks()),
                hoeren.getAudiogrammBeurteilung(),
                hoeren.getZusaetzlicheAngaben(),
                hoeren.getBemerkungen()
            )
        );
    }

    @Nullable
    public UntersuchungHoerenAudiogrammJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
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
        hoeren.setAudiogrammBeurteilung(saveJson.getAudiogrammBeurteilung());
        hoeren.setZusaetzlicheAngaben(saveJson.getZusaetzlicheAngaben());
        hoeren.setBemerkungen(saveJson.getBemerkungen());
        return hoeren;
    }

    @NonNull
    public UntersuchungHoerenAudiogrammJson saveAudiogramm(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungHoerenAudiogrammSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();
        UntersuchungHoerenAudiogramm entity = toEntity(untersuchung, hoerenAudiogramm, saveJson);
        return toJson(untersuchungHoerenService.persist(entity));
    }

    public UntersuchungHoerenAudiogrammSaveJson getDefaultUntersuchungHoerenAudiogrammForAnlassConfig(
        @NonNull ID<Anlass> anlassID) {
        AnlassConfig anlassConfig = anlassService.getById(anlassID).getAnlassConfig();
        return UntersuchungHoerenAudiogrammSaveJson.defaultAudiogrammValuesForAnlassConfig(AnlassConfigMapper.toJson(
            anlassConfig));
    }
}
