package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungDurchfuehrungSaveJson {

    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson arzt;

    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson mpa;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolle;

    @NotNull
    @Schema(required = true)
    private boolean nachzuegler;

    @NotNull
    @Schema(required = true)
    private boolean neueintritt;

}
