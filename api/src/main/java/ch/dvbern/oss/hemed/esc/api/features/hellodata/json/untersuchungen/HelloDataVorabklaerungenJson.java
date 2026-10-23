package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
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
public class HelloDataVorabklaerungenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    FragebogenStatus fragebogenStatus;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund fragebogenStatusNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    EinverstaendnisImpfungen einverstaendnisImpfungen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund einverstaendnisImpfungenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    ChronischeKrankheiten chronischeKrankheiten;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund chronischeKrankheitenNichtGeprueftGrund;
}
