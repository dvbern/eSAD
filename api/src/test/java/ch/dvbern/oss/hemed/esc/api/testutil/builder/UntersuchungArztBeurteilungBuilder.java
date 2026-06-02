package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Aussprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallFangen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallWerfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.FigurNachmalen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Hampelmann;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Kognition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Lesefit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.NameSchreiben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Stifthaltung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhaltensfragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZahlenNachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Zeichnen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZeichnenMensch;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztBeurteilungJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztBeurteilungSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
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
    public UntersuchungArztBeurteilungBuilder withEinverstaendniserklaerung(
        @Nullable EnumSet<Einverstaendniserklaerung> einverstaendniserklaerung
    ) {
        this.untersuchungArzt.setEinverstaendniserklaerung(einverstaendniserklaerung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withVerhaltensfragebogen(
        @Nullable Verhaltensfragebogen verhaltensfragebogen) {
        this.untersuchungArzt.setVerhaltensfragebogen(verhaltensfragebogen);
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
    public UntersuchungArztBeurteilungBuilder withBlutdruckSystolisch(@Nullable Integer blutdruckSystolisch) {
        this.untersuchungArzt.setBlutdruckSystolisch(blutdruckSystolisch);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBlutdruckDiastolisch(@Nullable Integer blutdruckDiastolisch) {
        this.untersuchungArzt.setBlutdruckDiastolisch(blutdruckDiastolisch);
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
    public UntersuchungArztBeurteilungBuilder withSprachfaehigkeit(
        @Nullable Collection<Sprachfaehigkeit> sprachfaehigkeit,
        @Nullable String bemerkung,
        @Nullable BigDecimal sprachfaehigkeitPunkte
    ) {
        this.untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSetUtil.toEnumSet(
            sprachfaehigkeit,
            Sprachfaehigkeit.class));
        if (sprachfaehigkeit != null) {
            this.untersuchungArzt.setSprachfaehigkeitSingleValue(sprachfaehigkeit.stream().findFirst().orElse(null));
        }
        this.untersuchungArzt.setSprachfaehigkeitBemerkung(bemerkung);
        this.untersuchungArzt.setSprachfaehigkeitPunkte(sprachfaehigkeitPunkte);
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
    public UntersuchungArztBeurteilungBuilder withMotorikPunkte(@Nullable BigDecimal motorikPunkte) {
        this.untersuchungArzt.setMotorikPunkte(motorikPunkte);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withKognition(
        @Nullable Kognition kognition,
        @Nullable BigDecimal kognitionPunkte
    ) {
        this.untersuchungArzt.setKognition(kognition);
        this.untersuchungArzt.setKognitionPunkte(kognitionPunkte);
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
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        this.untersuchungArzt.setEinbeinstandBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinstand(
        @Nullable Einbeinstand einbeinstand,
        @Nullable Einbeinstand einbeinstandLinks,
        @Nullable Einbeinstand einbeinstandRechts,
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        this.untersuchungArzt.setEinbeinstandLinks(einbeinstandLinks);
        this.untersuchungArzt.setEinbeinstandRechts(einbeinstandRechts);
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
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        this.untersuchungArzt.setEinbeinhuepfenBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withEinbeinhuepfen(
        @Nullable Einbeinhuepfen einbeinhuepfen,
        @Nullable Einbeinhuepfen einbeinhuepfenLinks,
        @Nullable Einbeinhuepfen einbeinhuepfenRechts,
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        this.untersuchungArzt.setEinbeinhuepfenLinks(einbeinhuepfenLinks);
        this.untersuchungArzt.setEinbeinhuepfenRechts(einbeinhuepfenRechts);
        this.untersuchungArzt.setEinbeinhuepfenBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withFingeropposition(
        @Nullable Collection<Fingeropposition> fingeropposition
    ) {
        this.untersuchungArzt.setFingeropposition(EnumSetUtil.toEnumSet(fingeropposition, Fingeropposition.class));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withFingeropposition(
        @Nullable Collection<Fingeropposition> fingeropposition,
        @Nullable String bemerkung
    ) {
        this.untersuchungArzt.setFingeropposition(EnumSetUtil.toEnumSet(fingeropposition, Fingeropposition.class));
        this.untersuchungArzt.setFingeroppositionBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withHampelmann(
        @Nullable Hampelmann hampelmann
    ) {
        this.untersuchungArzt.setHampelmann(hampelmann);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBallFangen(
        @Nullable BallFangen ballFangen
    ) {
        this.untersuchungArzt.setBallFangen(ballFangen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withBallWerfen(
        @Nullable BallWerfen ballWerfen
    ) {
        this.untersuchungArzt.setBallWerfen(ballWerfen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withNameSchreiben(
        @Nullable NameSchreiben nameSchreiben
    ) {
        this.untersuchungArzt.setNameSchreiben(nameSchreiben);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withFigurNachmalen(
        @Nullable FigurNachmalen figurNachmalen
    ) {
        this.untersuchungArzt.setFigurNachmalen(figurNachmalen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withStifthaltung(
        @Nullable Stifthaltung stifthaltung
    ) {
        this.untersuchungArzt.setStifthaltung(stifthaltung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withAussprache(
        @Nullable Aussprache aussprache
    ) {
        this.untersuchungArzt.setAussprache(aussprache);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withLesefit(
        @Nullable Lesefit lesefit,
        @Nullable BigDecimal lesefitPunkte
    ) {
        this.untersuchungArzt.setLesefit(lesefit);
        this.untersuchungArzt.setLesefitPunkte(lesefitPunkte);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withZahlenNachsprechen(
        @Nullable ZahlenNachsprechen zahlenNachsprechen4,
        @Nullable ZahlenNachsprechen zahlenNachsprechen5
    ) {
        this.untersuchungArzt.setZahlenNachsprechen4(zahlenNachsprechen4);
        this.untersuchungArzt.setZahlenNachsprechen5(zahlenNachsprechen5);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBeurteilungBuilder withZeichnen(
        @Nullable Zeichnen zeichnenKreis,
        @Nullable Zeichnen zeichnenViereck,
        @Nullable Zeichnen zeichnenDreieck,
        @Nullable ZeichnenMensch zeichnenMensch,
        @Nullable BigDecimal zeichnenMenschPunkte
    ) {
        this.untersuchungArzt.setZeichnenKreis(zeichnenKreis);
        this.untersuchungArzt.setZeichnenViereck(zeichnenViereck);
        this.untersuchungArzt.setZeichnenDreieck(zeichnenDreieck);
        this.untersuchungArzt.setZeichnenMensch(zeichnenMensch);
        this.untersuchungArzt.setZeichnenMenschPunkte(zeichnenMenschPunkte);
        return this;
    }

    @NonNull
    public static UntersuchungArztBeurteilung create(@NonNull Consumer<UntersuchungArztBeurteilungBuilder> block) {
        UntersuchungArztBeurteilungBuilder builder = new UntersuchungArztBeurteilungBuilder();
        block.accept(builder);
        return builder.untersuchungArzt;
    }

    @NonNull
    public static UntersuchungArztBeurteilungSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block
    ) {
        UntersuchungArztBeurteilung entity = create(block);
        return UntersuchungArztBeurteilungMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungArztBeurteilungJson createJax(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block
    ) {
        UntersuchungArztBeurteilung entity = create(block);
        return UntersuchungArztBeurteilungMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungArztBeurteilungSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block
    ) {
        UntersuchungArztBeurteilungSaveJson saveJson = createSaveJax(block);
        return UntersuchungArztBeurteilungSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungArztBeurteilungJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungArztBeurteilungBuilder> block) {
        UntersuchungArztBeurteilungJson json = createJax(block);
        return UntersuchungArztBeurteilungJsonSpecMapper.INSTANCE.untersuchungArztJsonToUntersuchungArztJsonSpec(json);
    }
}
