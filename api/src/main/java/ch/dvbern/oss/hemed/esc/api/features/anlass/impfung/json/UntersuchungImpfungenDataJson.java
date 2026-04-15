package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungImpfungenMassnahmenJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
@AllArgsConstructor
public class UntersuchungImpfungenDataJson {

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungenJson untersuchungImpfungen;

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungImpfungenMassnahmenJson massnahmen;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfstatusListSaveJson impfstatusList;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungInternListJson impfungInternList;

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungImpfungenDataMinimalJson dataMinimal;

}
