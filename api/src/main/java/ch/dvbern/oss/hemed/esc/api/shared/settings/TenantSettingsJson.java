package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class TenantSettingsJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<TenantSetting> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final TenantEnum tenant;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final TenantSettingSaveJson data;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FachRolle> fachrollen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FachRolle> delegierbareFachrollen;
}
