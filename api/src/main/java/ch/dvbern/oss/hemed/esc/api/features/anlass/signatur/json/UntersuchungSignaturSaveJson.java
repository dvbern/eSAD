package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json;

import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungSignaturSaveJson {

    @Valid
    @Nullable
    @Schema(required = false)
    private BenutzerOptionJson person;

    @Nullable
    @Schema(required = false)
    private LocalDateTime timestampVisiert;

    @Schema(required = true)
    private long version;
}
