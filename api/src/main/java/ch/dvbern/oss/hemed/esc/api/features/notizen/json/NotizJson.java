package ch.dvbern.oss.hemed.esc.api.features.notizen.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NotizJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Notiz> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final NotizSaveJson data;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final KindOptionJson kind;

}
