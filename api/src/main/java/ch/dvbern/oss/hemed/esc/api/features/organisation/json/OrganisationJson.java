package ch.dvbern.oss.hemed.esc.api.features.organisation.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class OrganisationJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Organisation> id;

    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NonNull
    @NotNull
    @Valid
    @Schema(required = true)
    private final OrganisationSaveJson data;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String verantwortlicherArztFullname;
}
