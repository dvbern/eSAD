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
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@JaxDTO
public class AnlassWithOpenSlotsOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private ID<Anlass> anlassId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String titel;

    @NotNull
    @Schema(required = true)
    private int offeneSlots;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate datum;

    @Nullable
    @Schema
    private LocalDateTime zeitVon;

    @Nullable
    @Schema
    private LocalDateTime zeitBis;

    @QueryProjection
    public AnlassWithOpenSlotsOptionJson(
        @NonNull UUID anlassUUID,
        @NonNull AnlassTyp anlassTyp,
        @NonNull String titel,
        int offeneSlots,
        @NonNull LocalDate datum,
        @Nullable LocalDateTime zeitVon,
        @Nullable  LocalDateTime zeitBis
    ) {
        this.anlassId =  ID.of(anlassUUID, Anlass.class);
        this.anlassTyp =  anlassTyp;
        this.titel =  titel;
        this.offeneSlots = offeneSlots;
        this.datum = datum;
        this.zeitVon = zeitVon;
        this.zeitBis = zeitBis;
    }
}
