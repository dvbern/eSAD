package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class BenutzerSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private String titel;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String fullName;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String username;

    @NotNull
    @NonNull
    @Schema(required = true)
    private EmailAddress email;

    @Nullable
    @Schema(required = false)
    private NormalizedPhoneNumber telefon;

    @NotNull
    @Schema(required = true)
    private boolean aktiv;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FachRolle> fachRollen;

    @Schema(required = true)
    private long version;
}
