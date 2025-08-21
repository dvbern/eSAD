package ch.dvbern.oss.hemed.esc.api.features.infomaterial.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedStringJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@JaxDTO
public class InfomaterialSaveJson {

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private TranslatedStringJson name;

    @NotNull
    @Schema(required = true)
    private boolean aktiv = true;
}
