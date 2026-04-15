package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class SystemRollenJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FachRolle> fachRollen;
}
