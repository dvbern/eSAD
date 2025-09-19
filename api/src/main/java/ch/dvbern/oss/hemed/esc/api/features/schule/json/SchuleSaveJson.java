package ch.dvbern.oss.hemed.esc.api.features.schule.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierOptionJson;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
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
public class SchuleSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Schema(required = true)
    private String schulhaus;

    @Nullable
    @Schema(required = false)
    private String schulstandort;

    @Nullable
    @Schema(required = false)
    private String schulkreis;

    @Nullable
    @Schema(required = false)
    @Valid
    private QuartierOptionJson quartier;

    @Nullable
    @Schema(required = false)
    private String schulhausnummer;

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
    private String bemerkungen;

    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson zustaendigArzt;

    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson zustaendigMPA;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus schuleAktivStatus;

    @Schema(required = true)
    private long version;
}
