package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
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
public class HelloDataBewegungsapparatJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Skoliose skoliose;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund skolioseNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Haltungsschwaeche haltungsschwaeche;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund haltungsschwaecheNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    VerkuerzungHamstringmuskulatur verkuerzungHamstringmuskulatur;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund verkuerzungHamstringmuskulaturNichtGeprueftGrund;
}
