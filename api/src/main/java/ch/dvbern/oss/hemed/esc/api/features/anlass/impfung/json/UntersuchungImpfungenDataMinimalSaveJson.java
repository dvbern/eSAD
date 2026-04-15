package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;
import jakarta.validation.Valid;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenDataMinimalSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson impfausweisGeprueft;

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson immunsuppression;

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson masernImpfstatus;

    @Nullable
    @Schema(required = false)
    private Integer masernAnzahlImpfungen;

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson hpvImpfstatus;

    @Nullable
    @Schema(required = false)
    private Integer hpvAnzahlImpfungen;

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson impfempfehlung;

    @NonNull
    @Valid
    @Schema(required = true)
    private ValueEnhancedJson impfungDurchgefuehrt;

    @Schema(required = true)
    private long version;
}
