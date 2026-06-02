package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenBemerkungFragebogenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenBemerkungFragebogenSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBemerkungFragebogenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBemerkungFragebogenSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungSehenBemerkungFragebogenBuilder {

    private UntersuchungSehenBemerkungFragebogen sehen;

    private UntersuchungSehenBemerkungFragebogenBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.sehen = defaultUntersuchungSehen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(sehen);
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogen defaultUntersuchungSehen(@NonNull LocalDate now) {
        return UntersuchungSehenBemerkungFragebogen.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBemerkungFragebogenBuilder forUntersuchungSehenBemerkungFragebogen(
        @NonNull UntersuchungSehenBemerkungFragebogen existing) {
        this.sehen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBemerkungFragebogenBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.sehen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBemerkungFragebogenBuilder withBemerkungFragebogen(@Nullable String bemerkung) {
        this.sehen.setBemerkungenFragebogen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogen create(
        @NonNull Consumer<UntersuchungSehenBemerkungFragebogenBuilder> block
    ) {
        UntersuchungSehenBemerkungFragebogenBuilder builder = new UntersuchungSehenBemerkungFragebogenBuilder();
        block.accept(builder);
        return builder.sehen;
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogenSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungSehenBemerkungFragebogenBuilder> block
    ) {
        UntersuchungSehenBemerkungFragebogen entity = create(block);
        return UntersuchungSehenBemerkungFragebogenMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogenJson createJax(
        @NonNull Consumer<UntersuchungSehenBemerkungFragebogenBuilder> block
    ) {
        UntersuchungSehenBemerkungFragebogen entity = create(block);
        return UntersuchungSehenBemerkungFragebogenMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogenSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungSehenBemerkungFragebogenBuilder> block
    ) {
        UntersuchungSehenBemerkungFragebogenSaveJson saveJson = createSaveJax(block);
        return UntersuchungSehenBemerkungFragebogenSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogenJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungSehenBemerkungFragebogenBuilder> block
    ) {
        UntersuchungSehenBemerkungFragebogenJson json = createJax(block);
        return UntersuchungSehenBemerkungFragebogenJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
