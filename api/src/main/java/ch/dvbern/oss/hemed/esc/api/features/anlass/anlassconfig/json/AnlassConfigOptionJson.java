/*
 * Copyright © 2025 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassConfigOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private ID<AnlassConfig> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate datumVon;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate datumBis;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String bezeichnung;

    @NotNull
    @Schema(required = true)
    private boolean anonym;

}
