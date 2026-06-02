package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungHoerenOtoskopieJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungHoerenOtoskopieSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungHoerenOtoskopieBuilder {

    private UntersuchungHoerenOtoskopie hoerenOtoskopie;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungHoerenOtoskopieBuilder() {
        this.hoerenOtoskopie = defaultUntersuchungHoerenOtoskopie(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(hoerenOtoskopie);
    }

    @NonNull
    public static UntersuchungHoerenOtoskopie defaultUntersuchungHoerenOtoskopie(@NonNull LocalDate now) {
        return UntersuchungHoerenOtoskopie.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder forUntersuchungHoerenOtoskopie(
        @NonNull UntersuchungHoerenOtoskopie existing) {
        this.hoerenOtoskopie = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.hoerenOtoskopie.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieRechts(
        @Nullable Collection<OtoskopieResultat> resultatList,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.hoerenOtoskopie.setOtoskopieRechts(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(resultatList, OtoskopieResultat.class), nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieRechts(
        @Nullable List<OtoskopieResultat> resultatList,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund,
        @Nullable String bemerkung
    ) {
        this.hoerenOtoskopie.setOtoskopieRechts(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(
                resultatList,
                OtoskopieResultat.class), nichtGeprueftGrund));
        this.hoerenOtoskopie.setOtoskopieRechtsResultatAndere(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieRechtsResultatAndere(
        @Nullable String otoskopieRechtsResultatAndere
    ) {
        this.hoerenOtoskopie.setOtoskopieRechtsResultatAndere(otoskopieRechtsResultatAndere);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieLinks(
        @Nullable Collection<OtoskopieResultat> resultatList,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.hoerenOtoskopie.setOtoskopieLinks(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(
                resultatList,
                OtoskopieResultat.class), nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieLinks(
        @Nullable Collection<OtoskopieResultat> resultatList,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund,
        @Nullable String bemerkung
    ) {
        this.hoerenOtoskopie.setOtoskopieLinks(MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(
                resultatList,
                OtoskopieResultat.class), nichtGeprueftGrund));
        this.hoerenOtoskopie.setOtoskopieLinksResultatAndere(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withOtoskopieLinksResultatAndere(
        @Nullable String otoskopieLinksResultatAndere
    ) {
        this.hoerenOtoskopie.setOtoskopieLinksResultatAndere(otoskopieLinksResultatAndere);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungHoerenOtoskopieBuilder withBemerkung(@Nullable String bemerkung) {
        this.hoerenOtoskopie.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    public static UntersuchungHoerenOtoskopie create(@NonNull Consumer<UntersuchungHoerenOtoskopieBuilder> block) {
        UntersuchungHoerenOtoskopieBuilder builder = new UntersuchungHoerenOtoskopieBuilder();
        block.accept(builder);
        return builder.hoerenOtoskopie;
    }

    @NonNull
    public static UntersuchungHoerenOtoskopieSaveJson createSaveJax(
        @NonNull Consumer<UntersuchungHoerenOtoskopieBuilder> block
    ) {
        UntersuchungHoerenOtoskopie entity = create(block);
        return UntersuchungHoerenOtoskopieMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungHoerenOtoskopieJson createJax(
        @NonNull Consumer<UntersuchungHoerenOtoskopieBuilder> block
    ) {
        UntersuchungHoerenOtoskopie entity = create(block);
        return UntersuchungHoerenOtoskopieMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungHoerenOtoskopieSaveJsonSpec createSaveJaxSpec(
        @NonNull Consumer<UntersuchungHoerenOtoskopieBuilder> block
    ) {
        UntersuchungHoerenOtoskopieSaveJson saveJson = createSaveJax(block);
        return UntersuchungHoerenOtoskopieSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(saveJson);
    }

    @NonNull
    public static UntersuchungHoerenOtoskopieJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungHoerenOtoskopieBuilder> block
    ) {
        UntersuchungHoerenOtoskopieJson json = createJax(block);
        return UntersuchungHoerenOtoskopieJsonSpecMapper.INSTANCE.
            untersuchungHoerenOtoskopieJsonToUntersuchungHoerenOtoskopieJsonSpec(json);
    }
}
