package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class BenutzerOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Benutzer> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String fullName;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FachRolle> fachrollen;

    @NotNull
    @Schema(required = true)
    private boolean aktiv;
}
