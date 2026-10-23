package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
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
public class HelloDataSchulrelevanteBeeintraechtigungJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    FreiesGehen freiesGehen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund freiesGehenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Integer anzahlMonateFreiesGehen;

    @Nullable
    @Schema(required = false)
    OhneStuetzradVelofahren ohneStuetzradVelofahren;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund ohneStuetzradVelofahrenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Grafomotorik grafomotorik;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund grafomotorikNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Feinmotorik feinmotorik;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund feinmotorikNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    GrobmotorikEinbeinhuepfen grobmotorikEinbeinhuepfen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund grobmotorikEinbeinhuepfenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    GleichgewichtEinbeinstand gleichgewichtEinbeinstand;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund gleichgewichtEinbeinstandNichtGeprueftGrund;
}
