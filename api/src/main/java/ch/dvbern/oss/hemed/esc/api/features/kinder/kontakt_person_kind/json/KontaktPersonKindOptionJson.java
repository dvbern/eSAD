package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KontaktPersonKindOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private ID<KontaktPersonKind> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String name;

    @Nullable
    @Schema(required = false)
    private String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String fullName;
}
