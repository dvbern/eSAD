package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungVorabklaerungenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungVorabklaerungenJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungVorabklaerungenBuilder {

    private UntersuchungVorabklaerungen vorabklaerungen;

    private UntersuchungVorabklaerungenBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.vorabklaerungen = defaultUntersuchungVorabklaerungen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(vorabklaerungen);
    }

    @NonNull
    public static UntersuchungVorabklaerungen defaultUntersuchungVorabklaerungen(@NonNull LocalDate now) {
        return UntersuchungVorabklaerungen.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungVorabklaerungenBuilder forUntersuchungVorabklaerungen(@NonNull UntersuchungVorabklaerungen existing) {
        this.vorabklaerungen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungVorabklaerungenBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.vorabklaerungen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungVorabklaerungenBuilder withFragebogenStatus(
        @Nullable FragebogenStatus status,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.vorabklaerungen.setFragebogenStatus(status == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(status, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungVorabklaerungenBuilder withEinverstaendnisImpfungen(
        @Nullable EinverstaendnisImpfungen einverstaendnis,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.vorabklaerungen.setEinverstaendnisImpfungen(einverstaendnis == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(einverstaendnis, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungVorabklaerungenBuilder withChronischeKrankheiten(
        @Nullable ChronischeKrankheiten chronischeKrankheiten,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.vorabklaerungen.setChronischeKrankheiten(chronischeKrankheiten == null
            ? SingleValueEnhanced.of()
            : SingleValueEnhanced.of(chronischeKrankheiten, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    public static UntersuchungVorabklaerungen create(@NonNull Consumer<UntersuchungVorabklaerungenBuilder> block) {
        UntersuchungVorabklaerungenBuilder builder = new UntersuchungVorabklaerungenBuilder();
        block.accept(builder);
        return builder.vorabklaerungen;
    }

    @NonNull
    public static UntersuchungVorabklaerungenJson createJax(@NonNull Consumer<UntersuchungVorabklaerungenBuilder> block) {
        UntersuchungVorabklaerungen entity = create(block);
        return UntersuchungVorabklaerungenMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungVorabklaerungenJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungVorabklaerungenBuilder> block) {
        UntersuchungVorabklaerungenJson json = createJax(block);
        return UntersuchungVorabklaerungenJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
