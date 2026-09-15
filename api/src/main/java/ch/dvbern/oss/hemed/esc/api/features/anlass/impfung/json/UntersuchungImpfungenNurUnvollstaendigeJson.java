package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenNurUnvollstaendige;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
@AllArgsConstructor
public class UntersuchungImpfungenNurUnvollstaendigeJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<UntersuchungImpfungenNurUnvollstaendige> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungenNurUnvollstaendigeSaveJson data;

}
