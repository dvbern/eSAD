package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
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
public class HelloDataSpracheJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Nachsprechen nachsprechen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund nachsprechenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Wortschatz wortschatz;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund wortschatzNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Sprachverstaendnis sprachverstaendnis;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund sprachverstaendnisNichtGeprueftGrund;
}
