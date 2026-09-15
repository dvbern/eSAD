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
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendige;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendigeMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenNurUnvollstaendigeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.UntersuchungImpfungenNurUnvollstaendigeJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenNurUnvollstaendigeJsonSpec;

import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UntersuchungImpfungenNurUnvollstaendigeBuilder {

    private final UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige;

    private UntersuchungImpfungenNurUnvollstaendigeBuilder() {
        FixedClockProvider clockProvider = FixedClockProvider.now();
        AppClock appClock = new AppClock(clockProvider);
        this.nurUnvollstaendige = defaultUntersuchungImpfungenNurUnvollstaendige(appClock.offsetDateTime().now().toLocalDate());
        TestUtil.setEntityInfo(nurUnvollstaendige);
    }

    @NonNull
    public static UntersuchungImpfungenNurUnvollstaendige defaultUntersuchungImpfungenNurUnvollstaendige(@NonNull LocalDate now) {
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungen.forUntersuchung(untersuchung);
        return UntersuchungImpfungenNurUnvollstaendige.forUntersuchung(impfungen);
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenNurUnvollstaendigeBuilder forUntersuchungImpfungen(@NonNull UntersuchungImpfungen impfungen) {
        this.nurUnvollstaendige.setUntersuchungImpfungen(impfungen);
        if (impfungen.getUntersuchungImpfungenNurUnvollstaendige() != null) {
            this.nurUnvollstaendige.setVersion(impfungen.getUntersuchungImpfungenNurUnvollstaendige().getVersion());
        } else {
            this.nurUnvollstaendige.setVersion(0L);
        }
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenNurUnvollstaendigeBuilder withImpfausweisVollstaendig(
        @Nullable ImpfausweisVollstaendig value
    ) {
        this.nurUnvollstaendige.setImpfausweisVollstaendig(value);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public UntersuchungImpfungenNurUnvollstaendigeBuilder withImpfausweisGrundUnvollstaendig(
        @Nullable ImpfausweisGrundUnvollstaendig value
    ) {
        this.nurUnvollstaendige.setImpfausweisGrundUnvollstaendig(value);
        return this;
    }

    @NonNull
    public static UntersuchungImpfungenNurUnvollstaendige create(@NonNull Consumer<UntersuchungImpfungenNurUnvollstaendigeBuilder> block) {
        UntersuchungImpfungenNurUnvollstaendigeBuilder builder = new UntersuchungImpfungenNurUnvollstaendigeBuilder();
        block.accept(builder);
        return builder.nurUnvollstaendige;
    }

    @NonNull
    public static UntersuchungImpfungenNurUnvollstaendigeJson createJax(@NonNull Consumer<UntersuchungImpfungenNurUnvollstaendigeBuilder> block) {
        UntersuchungImpfungenNurUnvollstaendige nurUnvollstaendige = create(block);
        return UntersuchungImpfungenNurUnvollstaendigeMapper.toJson(nurUnvollstaendige);
    }

    @NonNull
    public static UntersuchungImpfungenNurUnvollstaendigeJsonSpec createJaxSpec(@NonNull Consumer<UntersuchungImpfungenNurUnvollstaendigeBuilder> block) {
        UntersuchungImpfungenNurUnvollstaendigeJson json = createJax(block);
        return UntersuchungImpfungenNurUnvollstaendigeJsonSpecMapper.INSTANCE.jsonToJsonSpec(json);
    }
}
