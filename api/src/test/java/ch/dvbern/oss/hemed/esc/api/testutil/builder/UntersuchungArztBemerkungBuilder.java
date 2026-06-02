package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztBemerkungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztBemerkungSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungArztBemerkungBuilder {

    private UntersuchungArztBemerkung untersuchungArzt;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungArztBemerkungBuilder() {
        this.untersuchungArzt = defaultUntersuchungArzt(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(untersuchungArzt);
    }

    @NonNull
    public static UntersuchungArztBemerkung defaultUntersuchungArzt(@NonNull LocalDate date) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(date);
        return UntersuchungArztBemerkung.forUntersuchung(untersuchung);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBemerkungBuilder forUntersuchungArztBemerkung(@NonNull UntersuchungArztBemerkung existing) {
        this.untersuchungArzt = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBemerkungBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.untersuchungArzt.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBemerkungBuilder withBemerkungen(@Nullable String bemerkungen) {
        this.untersuchungArzt.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    public static UntersuchungArztBemerkung create(@NonNull Consumer<UntersuchungArztBemerkungBuilder> block) {
        UntersuchungArztBemerkungBuilder builder = new UntersuchungArztBemerkungBuilder();
        block.accept(builder);
        return builder.untersuchungArzt;
    }

    @NonNull
    public static UntersuchungArztBemerkungSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungArztBemerkungBuilder> block
    ) {
        UntersuchungArztBemerkung entity = create(block);
        return UntersuchungArztBemerkungMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungArztBemerkungJson createJax(@NonNull Consumer<UntersuchungArztBemerkungBuilder> block) {
        UntersuchungArztBemerkung entity = create(block);
        return UntersuchungArztBemerkungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungArztBemerkungSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungArztBemerkungBuilder> block
    ) {
        UntersuchungArztBemerkungSaveJson saveJson = createSaveJax(block);
        return UntersuchungArztBemerkungSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungArztBemerkungJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungArztBemerkungBuilder> block) {
        UntersuchungArztBemerkungJson json = createJax(block);
        return UntersuchungArztBemerkungJsonSpecMapper.INSTANCE.untersuchungArztBemerkungJsonToUntersuchungArztBemerkungJsonSpec(
            json);
    }
}
