package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztBeurteilungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungJsonSpec;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungArztBeurteilungBuilder {

    private UntersuchungArztBeurteilung untersuchungArzt;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungArztBeurteilungBuilder() {
        this.untersuchungArzt = defaultUntersuchungArzt(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(untersuchungArzt);
    }

    @NonNull
    public static UntersuchungArztBeurteilung defaultUntersuchungArzt(@NonNull LocalDate date) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(date);
        return UntersuchungArztBeurteilung.forUntersuchung(untersuchung);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder forUntersuchungArzt(@NonNull UntersuchungArztBeurteilung existing) {
        this.untersuchungArzt = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.untersuchungArzt.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withVerhalten(@Nullable Verhalten verhalten) {
        this.untersuchungArzt.setVerhalten(verhalten);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBlutdruck(@Nullable Blutdruck blutdruck) {
        this.untersuchungArzt.setBlutdruck(blutdruck);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withHerzauskultation(@Nullable Herzauskultation herzauskultation) {
        this.untersuchungArzt.setHerzauskultation(herzauskultation);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withMotorikPunkte(@Nullable BigDecimal motorikPunkte) {
        this.untersuchungArzt.setMotorikPunkte(motorikPunkte);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withHerzauskultation(
        @Nullable Herzauskultation herzauskultation,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setHerzauskultation(herzauskultation);
        this.untersuchungArzt.setHerzauskultationBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withHaut(@Nullable Haut haut) {
        this.untersuchungArzt.setHaut(haut);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withHaut(@Nullable Haut haut, @Nullable String bemerkung) {
        this.untersuchungArzt.setHaut(haut);
        this.untersuchungArzt.setHautBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withMundAndZaehne(@Nullable Collection<MundAndZaehne> mundAndZaehne) {
        this.untersuchungArzt.setMundAndZaehne(EnumSetUtil.toEnumSet(mundAndZaehne, MundAndZaehne.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withMundAndZaehne(
        @Nullable Collection<MundAndZaehne> mundAndZaehne,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setMundAndZaehne(EnumSetUtil.toEnumSet(mundAndZaehne, MundAndZaehne.class));
        this.untersuchungArzt.setMundAndZaehneBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withSprachfaehigkeit(
        @Nullable Collection<Sprachfaehigkeit> sprachfaehigkeit) {
        this.untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSetUtil.toEnumSet(
            sprachfaehigkeit,
            Sprachfaehigkeit.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withSprachfaehigkeit(
        @Nullable Collection<Sprachfaehigkeit> sprachfaehigkeit,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSetUtil.toEnumSet(
            sprachfaehigkeit,
            Sprachfaehigkeit.class));
        this.untersuchungArzt.setSprachfaehigkeitBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withMotorik(@Nullable Motorik motorik) {
        this.untersuchungArzt.setMotorik(motorik);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBewegungsapparat(
        @Nullable Collection<Bewegungsapparat> bewegungsapparat) {
        this.untersuchungArzt.setBewegungsapparat(EnumSetUtil.toEnumSet(bewegungsapparat, Bewegungsapparat.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBewegungsapparat(
        @Nullable Collection<Bewegungsapparat> bewegungsapparat,
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setBewegungsapparat(EnumSetUtil.toEnumSet(bewegungsapparat, Bewegungsapparat.class));
        this.untersuchungArzt.setBewegungsapparatBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinstand(@Nullable Einbeinstand einbeinstand) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinstand(
        @Nullable Einbeinstand einbeinstand,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        this.untersuchungArzt.setEinbeinstandBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinhuepfen(@Nullable Einbeinhuepfen einbeinhuepfen) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinhuepfen(
        @Nullable Einbeinhuepfen einbeinhuepfen,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        this.untersuchungArzt.setEinbeinhuepfenBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withFingeropposition(
        @Nullable Collection<Fingeropposition> fingeropposition) {
        this.untersuchungArzt.setFingeropposition(EnumSetUtil.toEnumSet(fingeropposition, Fingeropposition.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withFingeropposition(
        @Nullable Collection<Fingeropposition> fingeropposition,
        @Nullable String bemerkung) {
        this.untersuchungArzt.setFingeropposition(EnumSetUtil.toEnumSet(fingeropposition, Fingeropposition.class));
        this.untersuchungArzt.setFingeroppositionBemerkung(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungArztBeurteilung create(@NonNull Consumer<UntersuchungArztBeurteilungBuilder> block) {
        UntersuchungArztBeurteilungBuilder builder = new UntersuchungArztBeurteilungBuilder();
        block.accept(builder);
        return builder.untersuchungArzt;
    }

    @NonNull
    public static UntersuchungArztBeurteilungJson createJax(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block) {
        UntersuchungArztBeurteilung entity = create(block);
        return UntersuchungArztBeurteilungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungArztBeurteilungJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block) {
        UntersuchungArztBeurteilungJson json = createJax(block);
        return UntersuchungArztBeurteilungJsonSpecMapper.INSTANCE.untersuchungArztJsonToUntersuchungArztJsonSpec(json);
    }
}
