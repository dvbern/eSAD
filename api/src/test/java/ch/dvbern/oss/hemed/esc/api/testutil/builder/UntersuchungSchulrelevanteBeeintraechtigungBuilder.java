package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSchulrelevanteBeeintraechtigungSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungSchulrelevanteBeeintraechtigungBuilder {
    private UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung;

    private UntersuchungSchulrelevanteBeeintraechtigungBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.beeintraechtigung = defaultSchulrelevanteBeeintraechtigung(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(beeintraechtigung);
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigung defaultSchulrelevanteBeeintraechtigung(
        @NonNull
        LocalDate now) {
        return UntersuchungSchulrelevanteBeeintraechtigung.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder forUntersuchungSchulrelevanteBeeintraechtigung(
        @NonNull UntersuchungSchulrelevanteBeeintraechtigung existing) {
        this.beeintraechtigung = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.beeintraechtigung.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withFreiesGehen(
        @Nullable FreiesGehen freiesGehen,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setFreiesGehen(SingleValueEnhanced.of(freiesGehen, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withOhneStuetzradVelofahren(
        @Nullable OhneStuetzradVelofahren ohneStuetzradVelofahren,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setOhneStuetzradVelofahren(SingleValueEnhanced.of(ohneStuetzradVelofahren, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withGrafomotorik(
        @Nullable Grafomotorik grafomotorik,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setGrafomotorik(SingleValueEnhanced.of(grafomotorik, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withFeinmotorik(
        @Nullable Feinmotorik feinmotorik,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setFeinmotorik(SingleValueEnhanced.of(feinmotorik, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withGrobmotorik(
        @Nullable GrobmotorikEinbeinhuepfen grobmotorik,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setGrobmotorikEinbeinhuepfen(SingleValueEnhanced.of(grobmotorik, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withGleichgewicht(
        @Nullable GleichgewichtEinbeinstand gleichgewicht,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.beeintraechtigung.setGleichgewichtEinbeinstand(SingleValueEnhanced.of(gleichgewicht, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSchulrelevanteBeeintraechtigungBuilder withBemerkungen(@Nullable String bemerkungen) {
        this.beeintraechtigung.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigung create(@NonNull Consumer<UntersuchungSchulrelevanteBeeintraechtigungBuilder> block) {
        UntersuchungSchulrelevanteBeeintraechtigungBuilder builder = new UntersuchungSchulrelevanteBeeintraechtigungBuilder();
        block.accept(builder);
        return builder.beeintraechtigung;
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigungJson createJax(@NonNull Consumer<UntersuchungSchulrelevanteBeeintraechtigungBuilder> block) {
        UntersuchungSchulrelevanteBeeintraechtigung entity = create(block);
        return UntersuchungSchulrelevanteBeeintraechtigungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigungJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungSchulrelevanteBeeintraechtigungBuilder> block) {
        UntersuchungSchulrelevanteBeeintraechtigungJson json = createJax(block);
        return UntersuchungSchulrelevanteBeeintraechtigungSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
