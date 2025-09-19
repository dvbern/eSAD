package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenBeurteilungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungSehenBeurteilungBuilder {

    private UntersuchungSehenBeurteilung sehen;

    private UntersuchungSehenBeurteilungBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.sehen = defaultUntersuchungSehen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(sehen);
    }

    @NonNull
    public static UntersuchungSehenBeurteilung defaultUntersuchungSehen(@NonNull LocalDate now) {
        return UntersuchungSehenBeurteilung.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder forUntersuchungSehen(@NonNull UntersuchungSehenBeurteilung existing) {
        this.sehen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.sehen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withBrilleLinsen(@Nullable BrilleLinsenResultat brilleLinsen) {
        this.sehen.setBrilleLinsen(brilleLinsen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStereotest(@Nullable StereotestResultat stereotest) {
        this.sehen.setStereotest(stereotest);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFarbsehen(@Nullable FarbsehenResultat farbsehen) {
        this.sehen.setFarbsehen(farbsehen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withVerwendeteTafel(@Nullable VerwendetTafelResultat verwendeteTafel) {
        this.sehen.setVerwendeteTafel(verwendeteTafel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFernvisus(@Nullable List<DistanzSehenResultat> fernvisus) {
        this.sehen.setFernvisus(fernvisus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFernvisusLinks(@Nullable BigDecimal fernvisusLinks) {
        this.sehen.setFernvisusLinks(fernvisusLinks);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFernvisusRechts(@Nullable BigDecimal fernvisusRechts) {
        this.sehen.setFernvisusRechts(fernvisusRechts);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withNahvisus(@Nullable List<DistanzSehenResultat> nahvisus) {
        this.sehen.setNahvisus(nahvisus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withNahvisusLinks(@Nullable BigDecimal nahvisusLinks) {
        this.sehen.setNahvisusLinks(nahvisusLinks);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withNahvisusRechts(@Nullable BigDecimal nahvisusRechts) {
        this.sehen.setNahvisusRechts(nahvisusRechts);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withMotilitaet(@Nullable MotilitaetResultat motilitaet) {
        this.sehen.setMotilitaet(motilitaet);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismus(@Nullable List<StrabismusResultat> strabismus) {
        this.sehen.setStrabismus(strabismus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismusAuspraegung(
        @Nullable StrabismusAuspraegungResultat strabismusAuspraegung) {
        this.sehen.setStrabismusAuspraegung(strabismusAuspraegung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismusAuftreten(
        @Nullable StrabismusAuftretenResultat strabismusAuftreten) {
        this.sehen.setStrabismusAuftreten(strabismusAuftreten);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withInspektion(@Nullable InspektionKonvergenzResultat inspektion) {
        this.sehen.setInspektion(inspektion);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withKonvergenz(@Nullable InspektionKonvergenzResultat konvergenz) {
        this.sehen.setKonvergenz(konvergenz);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withBemerkung(@Nullable String bemerkung) {
        this.sehen.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungSehenBeurteilung create(@NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block) {
        UntersuchungSehenBeurteilungBuilder builder = new UntersuchungSehenBeurteilungBuilder();
        block.accept(builder);
        return builder.sehen;
    }

    @NonNull
    public static UntersuchungSehenBeurteilungJson createJax(@NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block) {
        UntersuchungSehenBeurteilung entity = create(block);
        return UntersuchungSehenBeurteilungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSehenBeurteilungJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block) {
        UntersuchungSehenBeurteilungJson json = createJax(block);
        return UntersuchungSehenBeurteilungJsonSpecMapper.INSTANCE.untersuchungSehenJsonToUntersuchungSehenJsonSpec(json);
    }
}
