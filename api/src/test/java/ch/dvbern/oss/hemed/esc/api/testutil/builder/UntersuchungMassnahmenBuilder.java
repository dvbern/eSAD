package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungSehenMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungMassnahmenJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungSehenMassnahmenSaveJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenSaveJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungMassnahmenBuilder {

    private UntersuchungMassnahmen massnahmen;
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private UntersuchungMassnahmenBuilder() {
        this.massnahmen = defaultUntersuchungMassnahmen(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(massnahmen);
    }

    @NonNull
    public static UntersuchungMassnahmen defaultUntersuchungMassnahmen(@NonNull LocalDate now) {
        UntersuchungMassnahmen entity =
            UntersuchungMassnahmen.forUntersuchung(UntersuchungBuilder.defaultUntersuchung(now));
        entity.setNachkontrolle(new AnlassFlags());
        entity.setAbgegebenesInfomaterial(Collections.emptySet());
        return entity;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder forUntersuchungMassnahmen(@NonNull UntersuchungMassnahmen existing) {
        this.massnahmen = existing;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder forUntersuchung(@NonNull Untersuchung untersuchung) {
        this.massnahmen.setUntersuchung(untersuchung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBemerkung(@NonNull String bemerkung) {
        this.massnahmen.setBemerkungen(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withKeineMassnahmen(boolean keineMassnahmen) {
        this.massnahmen.setKeineMassnahmen(keineMassnahmen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBeratungMPA(boolean beratungMPA) {
        this.massnahmen.setBeratungMPA(beratungMPA);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBeratungSchulAerztin(boolean beratungSchulAerztin) {
        this.massnahmen.setBeratungSchulAerztin(beratungSchulAerztin);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withInformationEltern(@Nullable InformationEltern informationEltern) {
        this.massnahmen.setInformationEltern(informationEltern);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withAnderes(boolean anderes) {
        this.massnahmen.setAnderes(anderes);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withAnderes(boolean anderes, @Nullable String bemerkung) {
        this.massnahmen.setAnderes(anderes);
        this.massnahmen.setAnderesBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBereitsBehandeltAnderes(boolean anderes) {
        this.massnahmen.setBereitsBehandeltAnderes(anderes);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBereitsBehandeltAnderes(boolean anderes, @Nullable String bemerkung) {
        this.massnahmen.setBereitsBehandeltAnderes(anderes);
        this.massnahmen.setBereitsBehandeltAnderesBemerkung(bemerkung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBereitsBehandeltHoeren(boolean hoeren) {
        this.massnahmen.setBereitsBehandeltHoeren(hoeren);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withBereitsBehandeltSehen(boolean hoeren) {
        this.massnahmen.setBereitsBehandeltSehen(hoeren);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withAbgegebenesInfomaterial(@NonNull Set<Infomaterial> infomaterial) {
        this.massnahmen.setAbgegebenesInfomaterial(infomaterial);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withUeberweisungHausarzt(boolean ueberweisung) {
        this.massnahmen.setUeberweisungHausarzt(ueberweisung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withUeberweisungAugenarzt(boolean ueberweisung) {
        this.massnahmen.setUeberweisungAugenarzt(ueberweisung);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungMassnahmenBuilder withNachkontrolle(
        boolean sehen,
        boolean hoeren,
        boolean groesseGewicht,
        boolean sprache,
        boolean motorik,
        boolean andere
    ) {
        this.massnahmen.getNachkontrolle().setNachkontrolleSehen(sehen);
        this.massnahmen.getNachkontrolle().setNachkontrolleHoeren(hoeren);
        this.massnahmen.getNachkontrolle().setNachkontrolleGroesseGewicht(groesseGewicht);
        this.massnahmen.getNachkontrolle().setNachkontrolleSprache(sprache);
        this.massnahmen.getNachkontrolle().setNachkontrolleMotorik(motorik);
        this.massnahmen.getNachkontrolle().setNachkontrolleAnderes(andere);
        return this;
    }

    @NonNull
    public static UntersuchungMassnahmen create(@NonNull Consumer<UntersuchungMassnahmenBuilder> block) {
        UntersuchungMassnahmenBuilder builder = new UntersuchungMassnahmenBuilder();
        block.accept(builder);
        return builder.massnahmen;
    }

    @NonNull
    public static UntersuchungMassnahmenJson createJax(@NonNull Consumer<UntersuchungMassnahmenBuilder> block) {
        UntersuchungMassnahmen entity = create(block);
        return UntersuchungMassnahmenMapper.toJson(entity);
    }

    @NonNull
    public static UntersuchungSehenMassnahmenSaveJson createSehenMassnahmenSaveJax(
        @NonNull Consumer<UntersuchungMassnahmenBuilder> block
    ) {
        UntersuchungMassnahmen entity = create(block);
        return UntersuchungSehenMassnahmenMapper.toSaveJson(entity);
    }

    @NonNull
    public static UntersuchungMassnahmenJsonSpec createJaxSpec(
        @NonNull Consumer<UntersuchungMassnahmenBuilder> block
    ) {
        UntersuchungMassnahmenJson json = createJax(block);
        return UntersuchungMassnahmenJsonSpecMapper.INSTANCE.jsonToJsonSpec(
            json);
    }

    @NonNull
    public static UntersuchungSehenMassnahmenSaveJsonSpec createSehenMassnahmenSaveJaxSpec(
        @NonNull Consumer<UntersuchungMassnahmenBuilder> block
    ) {
        UntersuchungSehenMassnahmenSaveJson saveJson = createSehenMassnahmenSaveJax(block);
        return UntersuchungSehenMassnahmenSaveJsonSpecMapper.INSTANCE.saveJsonToSaveJsonSpec(
            saveJson);
    }
}
