package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungHoerenAudiogrammJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungHoerenAudiogrammBuilder {

    private UntersuchungHoerenAudiogramm hoerenAudiogramm;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungHoerenAudiogrammBuilder() {
        this.hoerenAudiogramm = defaultUntersuchungHoerenAudiogramm(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(hoerenAudiogramm);
    }

    @NonNull
    public static UntersuchungHoerenAudiogramm defaultUntersuchungHoerenAudiogramm(@NonNull LocalDate now) {
        return UntersuchungHoerenAudiogramm.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder forUntersuchungHoerenAudiogramm(
        @NonNull UntersuchungHoerenAudiogramm existing) {
        this.hoerenAudiogramm = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.hoerenAudiogramm.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammRechts(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelAllHz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammRechts(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammLinks(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelAllHz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammLinks(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammRechtsWithout250Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout250Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammRechts(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammLinksWithout250Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout250Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammLinks(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammRechtsWithout8000Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout8000Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammRechts(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammLinksWithout8000Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout8000Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammLinks(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammRechtsWithout250HzAnd8000Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout250HzAnd8000Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammRechts(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammLinksWithout250HzAnd8000Hz(@Nullable Dezibel dezibel) {
        Audiogramm audiogramm = dezibel != null
            ? AudiogrammBuilder.create(builder -> builder.withDezibelWithout250HzAnd8000Hz(dezibel))
            : null;
        this.hoerenAudiogramm.setAudiogrammLinks(audiogramm);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withAudiogrammBeurteilung(
        @Nullable Collection<AudiogrammBeurteilung> beurteilungList
    ) {
        this.hoerenAudiogramm.setAudiogrammBeurteilung(EnumSetUtil.toEnumSet(beurteilungList, AudiogrammBeurteilung.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withZusaetzlicheAngaben(
        @Nullable Collection<HoerenZusaetzlicheAngaben> angabenList
    ) {
        this.hoerenAudiogramm.setZusaetzlicheAngaben(EnumSetUtil.toEnumSet(angabenList, HoerenZusaetzlicheAngaben.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withZusaetzlicheAngaben(
        @NonNull HoerenZusaetzlicheAngaben... zusaetzlicheAngaben
    ) {
        return this.withZusaetzlicheAngaben(List.of(zusaetzlicheAngaben));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withEmpfehlung(@Nullable Collection<HoerenEmpfehlung> empfehlungList) {
        this.hoerenAudiogramm.setEmpfehlung(EnumSetUtil.toEnumSet(empfehlungList, HoerenEmpfehlung.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenAudiogrammBuilder withBemerkung(@Nullable String bemerkung) {
        this.hoerenAudiogramm.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungHoerenAudiogramm create(@NonNull Consumer<UntersuchungHoerenAudiogrammBuilder> block) {
        UntersuchungHoerenAudiogrammBuilder builder = new UntersuchungHoerenAudiogrammBuilder();
        block.accept(builder);
        return builder.hoerenAudiogramm;
    }

    @NonNull
    public static UntersuchungHoerenAudiogrammJson createJax(@NonNull Consumer<UntersuchungHoerenAudiogrammBuilder> block) {
        UntersuchungHoerenAudiogramm entity = create(block);
        return UntersuchungHoerenAudiogrammMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungHoerenAudiogrammJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungHoerenAudiogrammBuilder> block) {
        UntersuchungHoerenAudiogrammJson json = createJax(block);
        return UntersuchungHoerenAudiogrammJsonSpecMapper.INSTANCE.untersuchungHoerenAudiogrammJsonToUntersuchungHoerenAudiogrammJsonSpec(
            json);
    }
}
