package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.NaechsteStation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungTriageSaveJson {

    @NotNull
    @Schema(required = true)
    private boolean triageSu;

    @Nullable
    @Schema(required = false)
    private String triageSuBemerkungen;

    @NotNull
    @Schema(required = true)
    private boolean triageMpa;

    @Nullable
    @Schema(required = false)
    private String triageMpaBemerkungen;

    @NotNull
    @Schema(required = true)
    private boolean befundSu;

    @Nullable
    @Schema(required = false)
    private String befundSuBemerkungen;

    @NotNull
    @Schema(required = true)
    private boolean wunschSchueler;

    @Nullable
    @Schema(required = false)
    private String wunschSchuelerBemerkungen;

    @NotNull
    @Schema(required = true)
    private boolean vorinformationLehrperson;

    @Nullable
    @Schema(required = false)
    private String vorinformationLehrpersonBemerkungen;

    @Nullable
    @Schema(required = false)
    private NaechsteStation naechsteStation;
}
