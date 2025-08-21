package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class PrincipalJson {

    @NonNull
    @Schema(required = true)
    @Valid
    BenutzerJson benutzerJson;

    @NonNull
    @Schema(required = true)
    @Valid
    List<Berechtigung> berechtigungen;

}
