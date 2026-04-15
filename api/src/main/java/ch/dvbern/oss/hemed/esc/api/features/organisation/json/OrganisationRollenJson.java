package ch.dvbern.oss.hemed.esc.api.features.organisation.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
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
public class OrganisationRollenJson {

    @Nullable
    @Schema(required = false)
    @Valid
    private OrganisationJson organisation;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private List<FachRolle> fachRollen;
}
