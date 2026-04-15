package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschlussMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungAbschlussJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungAbschlussJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungAbschlussBuilder {

    private UntersuchungAbschluss abschluss;

    private UntersuchungAbschlussBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.abschluss = defaultUntersuchungAbschluss(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(abschluss);
    }

    @NonNull
    public static UntersuchungAbschluss defaultUntersuchungAbschluss(@NonNull LocalDate now) {
        return UntersuchungAbschluss.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder forUntersuchungAbschluss(@NonNull UntersuchungAbschluss existing) {
        this.abschluss = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.abschluss.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder withUeberweisungErfolgt(
        @Nullable AbschlussResultat resultat,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.abschluss.setUeberweisungErfolgt(resultat == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(resultat, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder withAerztlicheValidierungErforderlich(
        @Nullable AbschlussResultat resultat,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.abschluss.setAerztlicheValidierungErforderlich(resultat == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(resultat, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder withBeratungDurchgefuehrt(
        @Nullable AbschlussResultat resultat,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.abschluss.setBeratungDurchgefuehrt(resultat == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(resultat, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungAbschlussBuilder withBeurteilungEmpfohleneMassnahmen(@Nullable String massnahmen) {
        this.abschluss.setBeurteilungEmpfohleneMassnahmen(massnahmen);
        return this;
    }

    @NonNull
    public static UntersuchungAbschluss create(@NonNull Consumer<UntersuchungAbschlussBuilder> block) {
        UntersuchungAbschlussBuilder builder = new UntersuchungAbschlussBuilder();
        block.accept(builder);
        return builder.abschluss;
    }

    @NonNull
    public static UntersuchungAbschlussJson createJax(@NonNull Consumer<UntersuchungAbschlussBuilder> block) {
        UntersuchungAbschluss entity = create(block);
        return UntersuchungAbschlussMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungAbschlussJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungAbschlussBuilder> block) {
        UntersuchungAbschlussJson json = createJax(block);
        return UntersuchungAbschlussJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
