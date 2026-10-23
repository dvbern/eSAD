package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataImpfungenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Schema(required = true)
    boolean impfausweisUnvollstaendig;

    @Schema(required = true)
    boolean impfausweisNichtVorhanden;

    @Schema(required = true)
    boolean impfausweisZweiPlusEinsSchema;

    @Schema(required = true)
    boolean checkEinwilligungVorhanden;

    @Schema(required = true)
    boolean checkKontraindikationenAbgeklaert;

    @Schema(required = true)
    boolean checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataImpfungenMassnahmenJson massnahmen;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataImpfungenNurUnvollstaendigeJson nurUnvollstaendige;
}
