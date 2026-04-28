package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;


import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

    @Nullable
    @Schema(required = false)
    @Valid
    private OrganisationRollenJson organisationRollen;

    @Nullable
    @Schema(required = false)
    @Valid
    private SystemRollenJson systemRollen;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus benutzerAktivStatus;
}
