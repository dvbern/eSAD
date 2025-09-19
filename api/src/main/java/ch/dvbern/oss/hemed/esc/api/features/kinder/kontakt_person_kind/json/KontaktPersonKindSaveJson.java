package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
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
public class KontaktPersonKindSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @Nullable
    @Schema(required = false)
    private final String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @Nullable
    @Schema(required = false)
    private KontaktPersonKindBeziehung beziehung;

    @Nullable
    @Schema(required = false)
    @Valid
    private AdresseJson adresse;

    @Nullable
    @Schema(required = false)
    private EmailAddress email;

    @Nullable
    @Schema(required = false)
    private NormalizedPhoneNumber telefon;

    @NotNull
    @Schema(required = true)
    private boolean korrespondenz;

    @Schema(required = true)
    private long version;
}
