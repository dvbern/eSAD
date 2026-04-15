package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSchulrelevanteBeeintraechtigungMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungSchulrelevanteBeeintraechtigungService untersuchungSchulrelevanteBeeintraechtigungService;

    @Nullable
    public static UntersuchungSchulrelevanteBeeintraechtigungJson toJson(
        @Nullable UntersuchungSchulrelevanteBeeintraechtigung beeintraechtung) {
        if (beeintraechtung == null) {
            return null;
        }
        return new UntersuchungSchulrelevanteBeeintraechtigungJson(
            beeintraechtung.getId(),
            EntityInfoJson.fromEntity(beeintraechtung),
            new UntersuchungSchulrelevanteBeeintraechtigungSaveJson(
                SingleValueEnhancedMapper.toJson(FreiesGehen.class, beeintraechtung.getFreiesGehen()),
                SingleValueEnhancedMapper.toJson(OhneStuetzradVelofahren.class, beeintraechtung.getOhneStuetzradVelofahren()),
                SingleValueEnhancedMapper.toJson(Grafomotorik.class, beeintraechtung.getGrafomotorik()),
                SingleValueEnhancedMapper.toJson(Feinmotorik.class, beeintraechtung.getFeinmotorik()),
                SingleValueEnhancedMapper.toJson(GrobmotorikEinbeinhuepfen.class, beeintraechtung.getGrobmotorikEinbeinhuepfen()),
                SingleValueEnhancedMapper.toJson(GleichgewichtEinbeinstand.class, beeintraechtung.getGleichgewichtEinbeinstand()),
                beeintraechtung.getBemerkungen(),
                beeintraechtung.getVersion()
            )
        );
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigung toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungSchulrelevanteBeeintraechtigung beeintraechtung,
        @NonNull UntersuchungSchulrelevanteBeeintraechtigungSaveJson saveJson
    ) {
        if (beeintraechtung == null) {
            beeintraechtung = UntersuchungSchulrelevanteBeeintraechtigung.forUntersuchung(untersuchung);
        }
        beeintraechtung.setFreiesGehen(SingleValueEnhancedMapper.toEntity(FreiesGehen.class, saveJson.getFreiesGehen()));
        beeintraechtung.setOhneStuetzradVelofahren(SingleValueEnhancedMapper.toEntity(OhneStuetzradVelofahren.class, saveJson.getOhneStuetzradVelofahren()));
        beeintraechtung.setGrafomotorik(SingleValueEnhancedMapper.toEntity(Grafomotorik.class, saveJson.getGrafomotorik()));
        beeintraechtung.setFeinmotorik(SingleValueEnhancedMapper.toEntity(Feinmotorik.class, saveJson.getFeinmotorik()));
        beeintraechtung.setGrobmotorikEinbeinhuepfen(SingleValueEnhancedMapper.toEntity(GrobmotorikEinbeinhuepfen.class, saveJson.getGrobmotorikEinbeinhuepfen()));
        beeintraechtung.setGleichgewichtEinbeinstand(SingleValueEnhancedMapper.toEntity(GleichgewichtEinbeinstand.class, saveJson.getGleichgewichtEinbeinstand()));
        beeintraechtung.setBemerkungen(saveJson.getBemerkungen());

        return beeintraechtung;
    }

    @Nullable
    public UntersuchungSchulrelevanteBeeintraechtigungJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            untersuchungSchulrelevanteBeeintraechtigungService.getByUntersuchung(untersuchung.getId());
        return beeintraechtigung != null ?
            toJson(beeintraechtigung) :
            null;
    }

    @NonNull
    public UntersuchungSchulrelevanteBeeintraechtigungJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungSchulrelevanteBeeintraechtigungSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung();
        UntersuchungSchulrelevanteBeeintraechtigung entity = toEntity(untersuchung, beeintraechtigung, saveJson);
        return toJson(untersuchungSchulrelevanteBeeintraechtigungService.persistWithOptimisticLocking(
            entity,
            saveJson.getVersion()));
    }
}
