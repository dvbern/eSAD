package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparatMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungBewegungsapparatJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungBewegungsapparatSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungBewegungsapparatBuilder {

    private UntersuchungBewegungsapparat bewegungsapparat;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungBewegungsapparatBuilder() {
        this.bewegungsapparat = defaultUntersuchungBewegungsapparat(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(bewegungsapparat);
    }

    @NonNull
    public static UntersuchungBewegungsapparat defaultUntersuchungBewegungsapparat(@NonNull LocalDate now) {
        return UntersuchungBewegungsapparat.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder forUntersuchungBewegungsapparat(
        @NonNull UntersuchungBewegungsapparat existing) {
        this.bewegungsapparat = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.bewegungsapparat.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder withSkoliose(
        @Nullable Skoliose skoliose,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.bewegungsapparat.setSkoliose(SingleValueEnhanced.of(skoliose, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder withHaltungsschwache(
        @Nullable Haltungsschwaeche haltungsschwaeche,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.bewegungsapparat.setHaltungsschwaeche(SingleValueEnhanced.of(haltungsschwaeche, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder withVerkuerzungHamstringmuskulatur(
        @Nullable VerkuerzungHamstringmuskulatur verkuerzungHamstringmuskulatur,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.bewegungsapparat.setVerkuerzungHamstringmuskulatur(SingleValueEnhanced.of(
            verkuerzungHamstringmuskulatur,
            nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungBewegungsapparatBuilder withBemerkung(
        @Nullable String bemerkung
    ) {
        this.bewegungsapparat.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungBewegungsapparat create(@NonNull Consumer<UntersuchungBewegungsapparatBuilder> block) {
        UntersuchungBewegungsapparatBuilder builder = new UntersuchungBewegungsapparatBuilder();
        block.accept(builder);
        return builder.bewegungsapparat;
    }

    @NonNull
    public static UntersuchungBewegungsapparatSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungBewegungsapparatBuilder> block) {
        UntersuchungBewegungsapparat entity = create(block);
        return UntersuchungBewegungsapparatMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungBewegungsapparatJson createJax(
        @NonNull Consumer<UntersuchungBewegungsapparatBuilder> block) {
        UntersuchungBewegungsapparat entity = create(block);
        return UntersuchungBewegungsapparatMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungBewegungsapparatSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungBewegungsapparatBuilder> block) {
        UntersuchungBewegungsapparatSaveJson saveJson = createSaveJax(block);
        return UntersuchungBewegungsapparatSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungBewegungsapparatJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungBewegungsapparatBuilder> block) {
        UntersuchungBewegungsapparatJson json = createJax(block);
        return UntersuchungBewegungsapparatJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
