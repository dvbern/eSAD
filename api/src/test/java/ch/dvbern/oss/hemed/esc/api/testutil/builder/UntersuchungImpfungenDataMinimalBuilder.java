/*
 * Copyright © 2025 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimalMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfungenDataMinimalJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenDataMinimalJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfungenDataMinimalBuilder {

    private final UntersuchungImpfungenDataMinimal dataMinimal;

    private UntersuchungImpfungenDataMinimalBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.dataMinimal = defaultUntersuchungImpfungenDataMinimal(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(dataMinimal);
    }

    @NonNull
    public static UntersuchungImpfungenDataMinimal defaultUntersuchungImpfungenDataMinimal(@NonNull LocalDate now) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungen.forUntersuchung(untersuchung);
        return UntersuchungImpfungenDataMinimal.forUntersuchung(impfungen);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder forUntersuchungImpfungen(@NonNull UntersuchungImpfungen impfungen) {
        this.dataMinimal.setUntersuchungImpfungen(impfungen);
        if (impfungen.getUntersuchungImpfungenDataMinimal() != null) {
            this.dataMinimal.setVersion(impfungen.getUntersuchungImpfungenDataMinimal().getVersion());
        } else {
            this.dataMinimal.setVersion(0L);
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withImpfausweisGeprueft(
        @Nullable ImpfausweisGeprueft value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setImpfausweisGeprueft(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withImmunsuppression(
        @Nullable Immunsuppression value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setImmunsuppression(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withMasernImpfstatus(
        @Nullable ImpfStatusMinimal value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setMasernImpfstatus(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withMasernAnzahlImpfungen(@Nullable Integer value) {
        this.dataMinimal.setMasernAnzahlImpfungen(value);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withHpvImpfstatus(
        @Nullable ImpfStatusMinimal value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setHpvImpfstatus(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withHpvAnzahlImpfungen(@Nullable Integer value) {
        this.dataMinimal.setHpvAnzahlImpfungen(value);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withImpfempfehlung(
        @Nullable ImpfempfehlungMinimal value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setImpfempfehlung(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenDataMinimalBuilder withImpfungDurchgefuehrt(
        @Nullable ImpfungDurchgefuehrtMinimal value,
        @Nullable NichtGeprueftGrund nichtGeprueftGrund
    ) {
        this.dataMinimal.setImpfungDurchgefuehrt(SingleValueEnhanced.of(value, nichtGeprueftGrund));
        return this;
    }

    @NonNull
    public static UntersuchungImpfungenDataMinimal create(@NonNull Consumer<UntersuchungImpfungenDataMinimalBuilder> block) {
        UntersuchungImpfungenDataMinimalBuilder builder = new UntersuchungImpfungenDataMinimalBuilder();
        block.accept(builder);
        return builder.dataMinimal;
    }

    @NonNull
    public static UntersuchungImpfungenDataMinimalJson createJax(@NonNull Consumer<UntersuchungImpfungenDataMinimalBuilder> block) {
        UntersuchungImpfungenDataMinimal dataMinimal = create(block);
        return UntersuchungImpfungenDataMinimalMapper.toJson(dataMinimal);
    }

    @NonNull
    public static UntersuchungImpfungenDataMinimalJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungImpfungenDataMinimalBuilder> block) {
        UntersuchungImpfungenDataMinimalJson json = createJax(block);
        return UntersuchungImpfungenDataMinimalJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
