/*
 * Copyright © 2025 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassWithOpenSlotsListJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<AnlassWithOpenSlotsOptionJson> anlaesseWithOpenEinladungenSlots;

    @NotNull
    @Schema(required = true)
    private int maximalAnzahlSlots;
}
