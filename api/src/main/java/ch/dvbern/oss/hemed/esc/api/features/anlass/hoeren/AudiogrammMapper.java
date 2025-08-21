package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
public final class AudiogrammMapper {

    private AudiogrammMapper() {
    }

    @Nullable
    public static AudiogrammJson toJson(@Nullable Audiogramm audiogramm) {
        if (audiogramm == null) {
            return null;
        }
        return new AudiogrammJson(
            audiogramm.getDbHz250(),
            audiogramm.getDbHz500(),
            audiogramm.getDbHz1000(),
            audiogramm.getDbHz2000(),
            audiogramm.getDbHz4000(),
            audiogramm.getDbHz6000(),
            audiogramm.getDbHz8000()
        );
    }

    @Nullable
    public static Audiogramm toEntity(
        @Nullable Audiogramm audiogramm,
        @Nullable AudiogrammJson saveJson
    ) {
        if (saveJson == null) {
            return null;
        }
        if (audiogramm == null) {
            audiogramm = new Audiogramm();
        }
        audiogramm.setDbHz250(saveJson.getDbHz250());
        audiogramm.setDbHz500(saveJson.getDbHz500());
        audiogramm.setDbHz1000(saveJson.getDbHz1000());
        audiogramm.setDbHz2000(saveJson.getDbHz2000());
        audiogramm.setDbHz4000(saveJson.getDbHz4000());
        audiogramm.setDbHz6000(saveJson.getDbHz6000());
        audiogramm.setDbHz8000(saveJson.getDbHz8000());
        return audiogramm;
    }
}
