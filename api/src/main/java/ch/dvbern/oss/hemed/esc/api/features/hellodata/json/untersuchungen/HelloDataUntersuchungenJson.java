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
public class HelloDataUntersuchungenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataKindDatenJson kindDaten;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataDurchfuehrungJson durchfuehrung;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataVorabklaerungenJson vorabklaerungen;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataTriageJson triage;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataImpfungenJson impfungen;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataSehenJson sehen;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataHoerenJson hoeren;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataGroesseGewichtJson groesseGewicht;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataArztJson arzt;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataSpracheJson sprache;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataBewegungsapparatJson bewegungsapparat;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataSchulrelevanteBeeintraechtigungJson beeintraechtigung;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataLaeusekontrolleJson laeusekontrolle;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataMassnahmenJson massnahmen;

    @Nullable
    @Valid
    @Schema(required = false)
    HelloDataAbschlussJson abschluss;
}
