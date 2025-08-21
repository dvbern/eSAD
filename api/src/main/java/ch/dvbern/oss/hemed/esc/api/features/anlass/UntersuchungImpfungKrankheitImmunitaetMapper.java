package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungKrankheitImmunitaetJson;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
public final class UntersuchungImpfungKrankheitImmunitaetMapper {

    private UntersuchungImpfungKrankheitImmunitaetMapper() {
    }

    @Nullable
    public static UntersuchungImpfungKrankheitImmunitaetJson toJson(@Nullable UntersuchungImpfungKrankheitImmunitaet immunitaet) {
        if (immunitaet == null) {
            return null;
        }
        return new UntersuchungImpfungKrankheitImmunitaetJson(
            immunitaet.getKrankheit(),
            immunitaet.isImmunitaetGenuegend()
        );
    }

    @NonNull
    public static UntersuchungImpfungKrankheitImmunitaet toEntity(
        @NonNull UntersuchungImpfungIntern impfung,
        @NonNull UntersuchungImpfungKrankheitImmunitaetJson saveJson
    ) {
        return new UntersuchungImpfungKrankheitImmunitaet(
            impfung,
            saveJson.getKrankheit(),
            saveJson.isImmunitaetGenuegend()
        );
    }
}
