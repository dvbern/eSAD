package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.AudiogrammJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AudiogrammBuilder {

    private final Audiogramm audiogramm;

    private AudiogrammBuilder() {
        this.audiogramm = defaultAudiogramm();
        TestUtil.setEntityInfo(audiogramm);
    }

    @NonNull
    public static Audiogramm defaultAudiogramm() {
        return new Audiogramm();
    }

    @NonNull
    @CanIgnoreReturnValue
    public AudiogrammBuilder withDezibelAllHz(@NonNull Dezibel dezibel) {
        this.audiogramm.setDbHz250(dezibel);
        this.audiogramm.setDbHz500(dezibel);
        this.audiogramm.setDbHz1000(dezibel);
        this.audiogramm.setDbHz2000(dezibel);
        this.audiogramm.setDbHz4000(dezibel);
        this.audiogramm.setDbHz6000(dezibel);
        this.audiogramm.setDbHz8000(dezibel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AudiogrammBuilder withDezibelWithout250Hz(@NonNull Dezibel dezibel) {
        this.audiogramm.setDbHz500(dezibel);
        this.audiogramm.setDbHz1000(dezibel);
        this.audiogramm.setDbHz2000(dezibel);
        this.audiogramm.setDbHz4000(dezibel);
        this.audiogramm.setDbHz6000(dezibel);
        this.audiogramm.setDbHz8000(dezibel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AudiogrammBuilder withDezibelWithout8000Hz(@NonNull Dezibel dezibel) {
        this.audiogramm.setDbHz250(dezibel);
        this.audiogramm.setDbHz500(dezibel);
        this.audiogramm.setDbHz1000(dezibel);
        this.audiogramm.setDbHz2000(dezibel);
        this.audiogramm.setDbHz4000(dezibel);
        this.audiogramm.setDbHz6000(dezibel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AudiogrammBuilder withDezibelWithout250HzAnd8000Hz(@NonNull Dezibel dezibel) {
        this.audiogramm.setDbHz500(dezibel);
        this.audiogramm.setDbHz1000(dezibel);
        this.audiogramm.setDbHz2000(dezibel);
        this.audiogramm.setDbHz4000(dezibel);
        this.audiogramm.setDbHz6000(dezibel);
        return this;
    }

    @NonNull
    public static Audiogramm create(@NonNull Consumer<AudiogrammBuilder> block) {
        AudiogrammBuilder builder = new AudiogrammBuilder();
        block.accept(builder);
        return builder.audiogramm;
    }

    @NonNull
    public static AudiogrammSaveJson createJax(@NonNull Consumer<AudiogrammBuilder> block) {
        Audiogramm entity = create(block);
        return AudiogrammMapper.toJson(entity);
    }

    @NonNull
    public static AudiogrammSaveJsonSpec createJaxSpec(@NonNull Consumer<AudiogrammBuilder> block) {
        AudiogrammSaveJson json = createJax(block);
        return AudiogrammJsonSpecMapper.INSTANCE.audiogrammJsonToAudiogrammJsonSpec(json);
    }
}
