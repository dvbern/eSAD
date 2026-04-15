package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSpracheMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSpracheJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSpracheJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungSpracheBuilder {

    private UntersuchungSprache sprache;

    private UntersuchungSpracheBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.sprache = defaultUntersuchungSprache(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(sprache);
    }

    @NonNull
    public static UntersuchungSprache defaultUntersuchungSprache(@NonNull LocalDate now) {
        return UntersuchungSprache.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder forUntersuchungSprache(@NonNull UntersuchungSprache existing) {
        this.sprache = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.sprache.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder withNachsprechen(
        @Nullable Nachsprechen nachsprechen,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sprache.setNachsprechen(SingleValueEnhanced.of(nachsprechen, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder withWortschatz(
        @Nullable Wortschatz wortschatz,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund) {
        this.sprache.setWortschatz(SingleValueEnhanced.of(wortschatz, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder withSprachverstaendnis(
        @Nullable Sprachverstaendnis sprachverstaendnis,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund) {
        this.sprache.setSprachverstaendnis(SingleValueEnhanced.of(sprachverstaendnis, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSpracheBuilder withBemerkung(@Nullable String bemerkung) {
        this.sprache.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungSprache create(@NonNull Consumer<UntersuchungSpracheBuilder> block) {
        UntersuchungSpracheBuilder builder = new UntersuchungSpracheBuilder();
        block.accept(builder);
        return builder.sprache;
    }

    @NonNull
    public static UntersuchungSpracheJson createJax(@NonNull Consumer<UntersuchungSpracheBuilder> block) {
        UntersuchungSprache entity = create(block);
        UntersuchungSpracheMapper mapper = new UntersuchungSpracheMapper(null, null);
        return mapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSpracheJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungSpracheBuilder> block) {
        UntersuchungSpracheJson json = createJax(block);
        return UntersuchungSpracheJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
