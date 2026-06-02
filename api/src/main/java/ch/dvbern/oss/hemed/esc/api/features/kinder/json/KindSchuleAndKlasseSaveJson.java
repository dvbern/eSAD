package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionJson;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KindSchuleAndKlasseSaveJson {

    @Nullable
    @Schema(required = false)
    @Valid
    private final SchuleOptionJson schule;

    @Nullable
    @Schema(required = false)
    @Valid
    private final KlasseOptionJson klasse;

    @Schema(required = true)
    private final long version;
}
