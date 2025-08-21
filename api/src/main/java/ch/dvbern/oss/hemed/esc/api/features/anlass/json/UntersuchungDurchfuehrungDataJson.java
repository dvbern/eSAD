package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungDurchfuehrungDataJson {

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungDurchfuehrungSaveJson initialValues;

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungDurchfuehrungJson untersuchungDurchfuehrung;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungKindDatenJson kindDaten;
}
