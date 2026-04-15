package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KlasseExternalBuilder {

    private final KlasseExternal klasseExternal;

    private KlasseExternalBuilder() {
        this.klasseExternal = defaultKlasseExternal();
    }

    public static KlasseExternal defaultKlasseExternal() {
        return new KlasseExternal(
            null,
            "343434",
            "PS  MA MARGA 1A",
            "Primarstufe",
            "RK",
            "1",
            "2",
            "Volksschulen");
    }

    @CanIgnoreReturnValue
    public KlasseExternalBuilder withKlasseBezeichnung(@Nullable String escadaKlasseBezeichnung) {
        klasseExternal.setKlasseBezeichnung(escadaKlasseBezeichnung);
        return this;
    }
}
