package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * This DTO can indicate if a Kind was saved successfully or if it was flagged as a potential duplicate.
 * Only one of the fields will be set
 */
@Value
@AllArgsConstructor
@JaxDTO
public class KindSaveResultJson {

    @Nullable
    @Schema(required = false)
    private final List<ID<Kind>> idsOfPotentialDuplicates;

    @Valid
    @Nullable
    @Schema(required = false)
    private final KindJson kindJson;

}
