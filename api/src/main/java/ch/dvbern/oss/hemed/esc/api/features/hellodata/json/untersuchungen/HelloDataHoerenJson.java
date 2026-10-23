package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataHoerenJson {

    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID audiogrammId;

    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID otoskopieId;

    @Nullable
    @Schema(required = false)
    AudiogrammSimpel audiogrammSimpel;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund audiogrammSimpelNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    HoerenHoerhilfe hoerhilfe;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund hoerhilfeNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<AudiogrammBeurteilung> audiogrammBeurteilung;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund audiogrammBeurteilungNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<HoerenZusaetzlicheAngaben> zusaetzlicheAngaben;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund zusaetzlicheAngabenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<HoerenEmpfehlung> empfehlung;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund empfehlungNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<OtoskopieResultat> otoskopieRechts;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund otoskopieRechtsNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<OtoskopieResultat> otoskopieLinks;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund otoskopieLinksNichtGeprueftGrund;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataAudiogrammJson audiogrammRechts;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataAudiogrammJson audiogrammLinks;
}
