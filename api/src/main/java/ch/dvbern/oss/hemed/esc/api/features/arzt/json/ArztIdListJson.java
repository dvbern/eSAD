package ch.dvbern.oss.hemed.esc.api.features.arzt.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Value
@JaxDTO
@AllArgsConstructor
public class ArztIdListJson {

    @NonNull
    @NotNull
    @Schema(required = true)
    private final List<ID<Arzt>> arztIdList;
}
