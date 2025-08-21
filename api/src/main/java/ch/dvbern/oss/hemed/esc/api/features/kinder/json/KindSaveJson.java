package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
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
@Schema(description = "Business data for a Kind")
public class KindSaveJson {
    @Nullable
    @Schema(required = false)
    private SozialversicherungsNummer sozialversicherungsNummer;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate geburtsdatum;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String alter;

    @Nullable
    @Schema(required = false)
    private Land nationalitaet;

    @Nullable
    @Schema(required = false)
    private String erstsprache;

    @Nullable
    @Schema(required = false)
    private String wohntyp;

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

    @Nullable
    @Schema(required = false)
    @Valid
    private final SchuleOptionJson schule;

    @Nullable
    @Schema(required = false)
    @Valid
    private final KlasseOptionJson klasse;

    @Nullable
    @Schema(required = false)
    private Klassenstufe klassenstufe;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus kindAktivStatus;
}
