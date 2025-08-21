package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztMapper;
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
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungArztJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztJsonSpec;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungArztBuilder {

    private UntersuchungArzt untersuchungArzt;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungArztBuilder() {
        this.untersuchungArzt = defaultUntersuchungArzt(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(untersuchungArzt);
    }

    @NonNull
    public static UntersuchungArzt defaultUntersuchungArzt(@NonNull LocalDate date) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(date);
        return UntersuchungArzt.forUntersuchung(untersuchung);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder forUntersuchungArzt(@NonNull UntersuchungArzt existing) {
        this.untersuchungArzt = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.untersuchungArzt.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withVerhalten(@Nullable Verhalten verhalten) {
        this.untersuchungArzt.setVerhalten(verhalten);
        return this;
    }



    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withBlutdruck(@Nullable Blutdruck blutdruck) {
        this.untersuchungArzt.setBlutdruck(blutdruck);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withHerzauskultation(@Nullable Herzauskultation herzauskultation) {
        this.untersuchungArzt.setHerzauskultation(herzauskultation);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withHerzauskultation(@Nullable Herzauskultation herzauskultation, @Nullable String bemerkung) {
        this.untersuchungArzt.setHerzauskultation(herzauskultation);
        this.untersuchungArzt.setHerzauskultationBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withHaut(@Nullable Haut haut) {
        this.untersuchungArzt.setHaut(haut);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withHaut(@Nullable Haut haut, @Nullable String bemerkung) {
        this.untersuchungArzt.setHaut(haut);
        this.untersuchungArzt.setHautBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withMundAndZaehne(@Nullable List<MundAndZaehne> mundAndZaehne) {
        this.untersuchungArzt.setMundAndZaehne(mundAndZaehne);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withMundAndZaehne(@Nullable List<MundAndZaehne> mundAndZaehne, @Nullable String bemerkung) {
        this.untersuchungArzt.setMundAndZaehne(mundAndZaehne);
        this.untersuchungArzt.setMundAndZaehneBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withSprachfaehigkeit(@Nullable List<Sprachfaehigkeit> sprachfaehigkeit) {
        this.untersuchungArzt.setSprachfaehigkeit(sprachfaehigkeit);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withSprachfaehigkeit(@Nullable List<Sprachfaehigkeit> sprachfaehigkeit, @Nullable String bemerkung) {
        this.untersuchungArzt.setSprachfaehigkeit(sprachfaehigkeit);
        this.untersuchungArzt.setSprachfaehigkeitBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withMotorik(@Nullable Motorik motorik) {
        this.untersuchungArzt.setMotorik(motorik);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withBewegungsapparat(@Nullable List<Bewegungsapparat> bewegungsapparat) {
        this.untersuchungArzt.setBewegungsapparat(bewegungsapparat);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withBewegungsapparat(@Nullable List<Bewegungsapparat> bewegungsapparat, @Nullable String bemerkung) {
        this.untersuchungArzt.setBewegungsapparat(bewegungsapparat);
        this.untersuchungArzt.setBewegungsapparatBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withEinbeinstand(@Nullable Einbeinstand einbeinstand) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withEinbeinstand(@Nullable Einbeinstand einbeinstand, @Nullable String bemerkung) {
        this.untersuchungArzt.setEinbeinstand(einbeinstand);
        this.untersuchungArzt.setEinbeinstandBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withEinbeinhuepfen(@Nullable Einbeinhuepfen einbeinhuepfen) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withEinbeinhuepfen(@Nullable Einbeinhuepfen einbeinhuepfen, @Nullable String bemerkung) {
        this.untersuchungArzt.setEinbeinhuepfen(einbeinhuepfen);
        this.untersuchungArzt.setEinbeinhuepfenBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withFingeropposition(@Nullable List<Fingeropposition> fingeropposition) {
        this.untersuchungArzt.setFingeropposition(fingeropposition);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withFingeropposition(@Nullable List<Fingeropposition> fingeropposition, @Nullable String bemerkung) {
        this.untersuchungArzt.setFingeropposition(fingeropposition);
        this.untersuchungArzt.setFingeroppositionBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungArztBuilder withBemerkungen(@Nullable String bemerkungen) {
        this.untersuchungArzt.setBemerkungen(bemerkungen);
        return this;
    }

    @NonNull
    public static UntersuchungArzt create(@NonNull Consumer<UntersuchungArztBuilder> block) {
        UntersuchungArztBuilder builder = new UntersuchungArztBuilder();
        block.accept(builder);
        return builder.untersuchungArzt;
    }

    @NonNull
    public static UntersuchungArztJson createJax(@NonNull Consumer<UntersuchungArztBuilder> block) {
        UntersuchungArzt entity = create(block);
        return UntersuchungArztMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungArztJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungArztBuilder> block) {
        UntersuchungArztJson json = createJax(block);
        return UntersuchungArztJsonSpecMapper.INSTANCE.untersuchungArztJsonToUntersuchungArztJsonSpec(json);
    }
}
