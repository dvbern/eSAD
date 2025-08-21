package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenJson;
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
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungSehenBuilder {

    private UntersuchungSehen sehen;

    private UntersuchungSehenBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.sehen = defaultUntersuchungSehen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(sehen);
    }

    @NonNull
    public static UntersuchungSehen defaultUntersuchungSehen(@NonNull LocalDate now) {
        return UntersuchungSehen.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder forUntersuchungSehen(@NonNull UntersuchungSehen existing) {
        this.sehen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.sehen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withBrilleLinsen(@Nullable BrilleLinsenResultat brilleLinsen) {
        this.sehen.setBrilleLinsen(brilleLinsen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withStereotest(@Nullable StereotestResultat stereotest) {
        this.sehen.setStereotest(stereotest);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withFarbsehen(@Nullable FarbsehenResultat farbsehen) {
        this.sehen.setFarbsehen(farbsehen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withVerwendeteTafel(@Nullable VerwendetTafelResultat verwendeteTafel) {
        this.sehen.setVerwendeteTafel(verwendeteTafel);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withFernvisus(@Nullable List<DistanzSehenResultat> fernvisus) {
        this.sehen.setFernvisus(fernvisus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withFernvisusLinks(@Nullable BigDecimal fernvisusLinks) {
        this.sehen.setFernvisusLinks(fernvisusLinks);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withFernvisusRechts(@Nullable BigDecimal fernvisusRechts) {
        this.sehen.setFernvisusRechts(fernvisusRechts);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withNahvisus(@Nullable List<DistanzSehenResultat> nahvisus) {
        this.sehen.setNahvisus(nahvisus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withNahvisusLinks(@Nullable BigDecimal nahvisusLinks) {
        this.sehen.setNahvisusLinks(nahvisusLinks);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withNahvisusRechts(@Nullable BigDecimal nahvisusRechts) {
        this.sehen.setNahvisusRechts(nahvisusRechts);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withMotilitaet(@Nullable MotilitaetResultat motilitaet) {
        this.sehen.setMotilitaet(motilitaet);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withStrabismus(@Nullable List<StrabismusResultat> strabismus) {
        this.sehen.setStrabismus(strabismus);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withStrabismusAuspraegung(
        @Nullable StrabismusAuspraegungResultat strabismusAuspraegung) {
        this.sehen.setStrabismusAuspraegung(strabismusAuspraegung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withStrabismusAuftreten(
        @Nullable StrabismusAuftretenResultat strabismusAuftreten) {
        this.sehen.setStrabismusAuftreten(strabismusAuftreten);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withInspektion(@Nullable InspektionKonvergenzResultat inspektion) {
        this.sehen.setInspektion(inspektion);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withKonvergenz(@Nullable InspektionKonvergenzResultat konvergenz) {
        this.sehen.setKonvergenz(konvergenz);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBuilder withBemerkung(@Nullable String bemerkung) {
        this.sehen.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungSehen create(@NonNull Consumer<UntersuchungSehenBuilder> block) {
        UntersuchungSehenBuilder builder = new UntersuchungSehenBuilder();
        block.accept(builder);
        return builder.sehen;
    }

    @NonNull
    public static UntersuchungSehenJson createJax(@NonNull Consumer<UntersuchungSehenBuilder> block) {
        UntersuchungSehen entity = create(block);
        return UntersuchungSehenMapper.toJson(entity, null);
    }

    @NonNull
    public static UntersuchungSehenJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungSehenBuilder> block) {
        UntersuchungSehenJson json = createJax(block);
        return UntersuchungSehenJsonSpecMapper.INSTANCE.untersuchungSehenJsonToUntersuchungSehenJsonSpec(json);
    }
}
