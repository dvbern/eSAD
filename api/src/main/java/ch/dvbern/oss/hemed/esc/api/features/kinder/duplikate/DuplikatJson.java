package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class DuplikatJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Duplikat> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private DuplikatKindJson kind1;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private DuplikatKindJson kind2;

    @NotNull
    @NonNull
    @Schema(required = true)
    private DuplikatStatus duplikatStatus;
}
