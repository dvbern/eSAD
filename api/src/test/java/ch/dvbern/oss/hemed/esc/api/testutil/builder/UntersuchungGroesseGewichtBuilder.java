package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungGroesseGewichtJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungGroesseGewichtBuilder {

    private UntersuchungGroesseGewicht groesseGewicht;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungGroesseGewichtBuilder() {
        this.groesseGewicht = defaultUntersuchungGroesseGewicht(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(groesseGewicht);
    }

    @NonNull
    public static UntersuchungGroesseGewicht defaultUntersuchungGroesseGewicht(@NonNull LocalDate now) {
        return UntersuchungGroesseGewicht.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder forUntersuchungGroesseGewicht(
        @NonNull UntersuchungGroesseGewicht existing) {
        this.groesseGewicht = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.groesseGewicht.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withGroesse(@Nullable Integer groesse) {
        this.groesseGewicht.setGroesse(groesse);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withGewicht(@Nullable BigDecimal gewicht) {
        this.groesseGewicht.setGewicht(gewicht);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withBmi(@Nullable BigDecimal bmi) {
        this.groesseGewicht.setBmi(bmi);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withGroessePerzentile(@Nullable PerzentileGroesse perzentile) {
        this.groesseGewicht.setGroessePerzentile(perzentile);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withGewichtPerzentile(@Nullable PerzentileGewicht perzentile) {
        this.groesseGewicht.setGewichtPerzentile(perzentile);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withBmiPerzentile(@Nullable PerzentileBmi perzentile) {
        this.groesseGewicht.setBmiPerzentile(perzentile);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungGroesseGewichtBuilder withBemerkung(@Nullable String bemerkung) {
        this.groesseGewicht.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungGroesseGewicht create(@NonNull Consumer<UntersuchungGroesseGewichtBuilder> block) {
        UntersuchungGroesseGewichtBuilder builder = new UntersuchungGroesseGewichtBuilder();
        block.accept(builder);
        return builder.groesseGewicht;
    }

    @NonNull
    public static UntersuchungGroesseGewichtJson createJax(@NonNull Consumer<UntersuchungGroesseGewichtBuilder> block) {
        UntersuchungGroesseGewicht entity = create(block);
        return UntersuchungGroesseGewichtMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungGroesseGewichtJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungGroesseGewichtBuilder> block) {
        UntersuchungGroesseGewichtJson json = createJax(block);
        return UntersuchungGroesseGewichtJsonSpecMapper.INSTANCE.untersuchungGroesseGewichtJsonToUntersuchungGroesseGewichtJsonSpec(
            json);
    }
}
