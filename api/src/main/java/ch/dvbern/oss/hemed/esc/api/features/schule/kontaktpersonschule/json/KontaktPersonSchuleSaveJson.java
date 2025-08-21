package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.KontaktPersonSchuleFunktion;
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
public class KontaktPersonSchuleSaveJson {

    @NonNull
    @NotNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @NonNull
    @NotNull
    @Schema(required = true)
    private KontaktPersonSchuleFunktion funktion;

    @NonNull
    @NotNull
    @Schema(required = true)
    private String name;

    @Nullable
    @Schema(required = false)
    private String vorname;

    @Valid
    @Nullable
    @Schema(required = false)
    private EmailAddress email;

    @Valid
    @Nullable
    @Schema(required = false)
    private NormalizedPhoneNumber telefon;
}
