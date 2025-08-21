package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KlasseOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Klasse> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String bezeichnung;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus aktivStatus;

}
