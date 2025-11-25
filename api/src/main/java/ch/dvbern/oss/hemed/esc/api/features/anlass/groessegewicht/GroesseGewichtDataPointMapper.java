package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.GroesseGewichtDataPointJson;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
public final class GroesseGewichtDataPointMapper {

    private GroesseGewichtDataPointMapper() {
    }

    @Nullable
    public static GroesseGewichtDataPointJson toJson(@Nullable UntersuchungGroesseGewicht groesseGewicht) {
        if (groesseGewicht == null) {
            return null;
        }
        return new GroesseGewichtDataPointJson(
            groesseGewicht.getGroesse(),
            groesseGewicht.getGewicht(),
            groesseGewicht.getAlter(),
            groesseGewicht.getAlterDuodezimal(),
            groesseGewicht.getBmi()
        );
    }
}
