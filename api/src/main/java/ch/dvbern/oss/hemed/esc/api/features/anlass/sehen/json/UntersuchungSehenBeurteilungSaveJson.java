package ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungSehenBeurteilungSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson brilleLinsen;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson stereotest;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson farbsehen;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson verwendeteTafel;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson fernvisus;

    @Nullable
    @Schema(required = false)
    private BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    private BigDecimal fernvisusLinks;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson fernvisusAugeLinks;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson fernvisusAugeRechts;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson nahvisus;

    @Nullable
    @Schema(required = false)
    private BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    private BigDecimal nahvisusLinks;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson motilitaet;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson strabismus;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson strabismusAuspraegung;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson strabismusAuftreten;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson inspektion;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson konvergenz;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson empfehlung;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;

}
