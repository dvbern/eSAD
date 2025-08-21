package ch.dvbern.oss.hemed.esc.api.shared.mandant;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class TranslatedStringJson {

    @Nullable
    @Schema(required = false)
    private final String de;

    @Nullable
    @Schema(required = false)
    private final String fr;

}
