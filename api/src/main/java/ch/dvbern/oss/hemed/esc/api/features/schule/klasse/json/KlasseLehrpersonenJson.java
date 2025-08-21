package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonOptionJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KlasseLehrpersonenJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private KlasseOptionJson klasseOptionJson;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<LehrpersonOptionJson> lehrpersonOptions;
}
