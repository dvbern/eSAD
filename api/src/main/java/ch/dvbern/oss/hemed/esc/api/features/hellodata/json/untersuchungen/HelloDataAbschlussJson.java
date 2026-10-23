package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataAbschlussJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    AbschlussResultat ueberweisungErfolgt;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund ueberweisungErfolgtNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    AbschlussResultat aerztlicheValidierungErforderlich;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund aerztlicheValidierungErforderlichNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    AbschlussResultat beratungDurchgefuehrt;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund beratungDurchgefuehrtNichtGeprueftGrund;
}
