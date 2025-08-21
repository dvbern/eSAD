package ch.dvbern.oss.hemed.esc.api.shared.person;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AdresseJson {

    @Nullable
    @Schema(required = false)
    private final String strasseUndNr;

    @Nullable
    @Schema(required = false)
    private final String adresseZusatz;

    @Nullable
    @Schema(required = false)
    private final String plz;

    @Nullable
    @Schema(required = false)
    private final String ort;

    @Nullable
    @Schema(required = false)
    private Land land;

}
