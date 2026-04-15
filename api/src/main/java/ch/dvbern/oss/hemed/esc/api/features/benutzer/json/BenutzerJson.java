package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
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
public class BenutzerJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Benutzer> id;

    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NonNull
    @NotNull
    @Valid
    @Schema(required = true)
    private final BenutzerSaveJson data;

    @Nullable
    @Schema(required = false)
    private OffsetDateTime lastUsedSessionTimestamp;

    @Nullable
    @Schema(required = false)
    private OffsetDateTime timestampAGBAccepted;

}
