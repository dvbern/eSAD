package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenBeurteilungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenBeurteilungSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
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
    public UntersuchungSehenBeurteilungBuilder withBrilleLinsen(
        @Nullable BrilleLinsenResultat brilleLinsen,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setBrilleLinsen(SingleValueEnhanced.of(brilleLinsen, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStereotest(
        @Nullable StereotestResultat stereotest,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setStereotest(SingleValueEnhanced.of(stereotest, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFarbsehen(
        @Nullable FarbsehenResultat farbsehen,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setFarbsehen(SingleValueEnhanced.of(farbsehen, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withVerwendeteTafel(
        @Nullable VerwendetTafelResultat verwendeteTafel,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setVerwendeteTafel(SingleValueEnhanced.of(verwendeteTafel, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withFernvisus(
        @Nullable Collection<DistanzSehenResultat> fernvisus,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setFernvisus(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(fernvisus, DistanzSehenResultat.class),
            nichtGeprueftGrund));
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
    public UntersuchungSehenBeurteilungBuilder withFernvisusAuge(
        @Nullable DistanzSehenAugeEinzelnResultat fernvisusLinks,
        @Nullable DistanzSehenAugeEinzelnResultat fernvisusRechts,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setFernvisusAugeLinks(SingleValueEnhanced.of(
            fernvisusLinks,
            nichtGeprueftGrund));
        this.sehen.setFernvisusAugeRechts(SingleValueEnhanced.of(
            fernvisusRechts,
            nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withNahvisus(
        @Nullable Collection<DistanzSehenResultat> nahvisus,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setNahvisus(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(nahvisus, DistanzSehenResultat.class),
            nichtGeprueftGrund));
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
    public UntersuchungSehenBeurteilungBuilder withMotilitaet(
        @Nullable MotilitaetResultat motilitaet,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setMotilitaet(SingleValueEnhanced.of(motilitaet, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismus(
        @Nullable Collection<StrabismusResultat> strabismus,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setStrabismus(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(strabismus, StrabismusResultat.class),
            nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismusAuspraegung(
        @Nullable StrabismusAuspraegungResultat strabismusAuspraegung,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setStrabismusAuspraegung(SingleValueEnhanced.of(strabismusAuspraegung, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withStrabismusAuftreten(
        @Nullable StrabismusAuftretenResultat strabismusAuftreten,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setStrabismusAuftreten(SingleValueEnhanced.of(strabismusAuftreten, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withInspektion(
        @Nullable InspektionKonvergenzResultat inspektion,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setInspektion(SingleValueEnhanced.of(inspektion, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withKonvergenz(
        @Nullable InspektionKonvergenzResultat konvergenz,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setKonvergenz(SingleValueEnhanced.of(konvergenz, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungSehenBeurteilungBuilder withEmpfehlung(
        @Nullable Collection<SehenEmpfehlung> empfehlungList,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.sehen.setEmpfehlung(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(empfehlungList, SehenEmpfehlung.class),
            nichtGeprueftGrund));
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
    public static UntersuchungSehenBeurteilungSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block
    ) {
        UntersuchungSehenBeurteilung entity = create(block);
        return UntersuchungSehenBeurteilungMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungSehenBeurteilungJson createJax(
        @NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block
    ) {
        UntersuchungSehenBeurteilung entity = create(block);
        return UntersuchungSehenBeurteilungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSehenBeurteilungSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block
    ) {
        UntersuchungSehenBeurteilungSaveJson saveJson = createSaveJax(block);
        return UntersuchungSehenBeurteilungSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungSehenBeurteilungJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungSehenBeurteilungBuilder> block
    ) {
        UntersuchungSehenBeurteilungJson json = createJax(block);
        return UntersuchungSehenBeurteilungJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
