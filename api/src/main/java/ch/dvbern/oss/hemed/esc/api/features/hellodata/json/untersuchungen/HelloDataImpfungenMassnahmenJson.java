package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataImpfungenMassnahmenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Schema(required = true)
    boolean keineMassnahmen;

    @Schema(required = true)
    boolean vormerkenFuerGruppenFolgetermin;

    @Schema(required = true)
    boolean impfberatungMpaDurchgefuehrt;

    @Schema(required = true)
    boolean infomaterialAbgegeben;

    @Nullable
    @Schema(required = false)
    InformationEltern informationEltern;

    @Schema(required = true)
    boolean informationHausarzt;

    @Schema(required = true)
    boolean rueckfrageHausarzt;

    @Nullable
    @Schema(required = false)
    Set<ImpfungenMassnahmenEmpfehlung> empfehlung;
}
