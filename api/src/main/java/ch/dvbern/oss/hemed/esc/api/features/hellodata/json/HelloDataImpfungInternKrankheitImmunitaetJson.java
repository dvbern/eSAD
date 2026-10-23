package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
public record HelloDataImpfungInternKrankheitImmunitaetJson(
    // Nullable because a LEFT JOIN returns null when no disease record exists.
    @Nullable
    @Schema(required = true)
    Krankheit krankheit,

    @Schema(required = true)
    boolean immunitaetGenuegend
) {

    public HelloDataImpfungInternKrankheitImmunitaetJson(
        @Nullable Krankheit krankheit,
        @Nullable Boolean immunitaetGenuegend
    ) {
        this(
            krankheit,
            Boolean.TRUE.equals(immunitaetGenuegend)
        );
    }
}
