package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class HelloDataImpfungInternJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID untersuchungId;

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID untersuchungImpfungenId;

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID impfstoffId;

    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID chargeId;

    @Nullable
    @Schema(required = false)
    LocalDate datumImpfung;

    @NotNull
    @NonNull
    @Schema(required = true)
    ImpfStatus impfStatus;

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID anlassImpfungErfasstId;

    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID anlassImpfungDurchgefuehrtId;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    List<HelloDataImpfungInternKrankheitImmunitaetJson> krankheitenImmunitaeten;

    @JsonCreator
    public HelloDataImpfungInternJson(
        @NonNull UUID id,
        @NonNull UUID untersuchungId,
        @NonNull UUID untersuchungImpfungenId,
        @NonNull UUID impfstoffId,
        @Nullable UUID chargeId,
        @Nullable LocalDate datumImpfung,
        @NonNull ImpfStatus impfStatus,
        @NonNull UUID anlassImpfungErfasstId,
        @Nullable UUID anlassImpfungDurchgefuehrtId,
        @Nullable List<HelloDataImpfungInternKrankheitImmunitaetJson> krankheitenImmunitaeten
    ) {
        this.id = Objects.requireNonNull(id);
        this.untersuchungId = Objects.requireNonNull(untersuchungId);
        this.untersuchungImpfungenId = Objects.requireNonNull(untersuchungImpfungenId);
        this.impfstoffId = Objects.requireNonNull(impfstoffId);
        this.chargeId = chargeId;
        this.datumImpfung = datumImpfung;
        this.impfStatus = Objects.requireNonNull(impfStatus);
        this.anlassImpfungErfasstId = Objects.requireNonNull(anlassImpfungErfasstId);
        this.anlassImpfungDurchgefuehrtId = anlassImpfungDurchgefuehrtId;
        this.krankheitenImmunitaeten = Objects.requireNonNullElse(krankheitenImmunitaeten, List.of());
    }

    public HelloDataImpfungInternJson(
        @NonNull UUID id,
        @NonNull UUID untersuchungId,
        @NonNull UUID untersuchungImpfungenId,
        @NonNull UUID impfstoffId,
        @Nullable UUID chargeId,
        @Nullable LocalDate datumImpfung,
        @NonNull ImpfStatus impfStatus,
        @NonNull UUID anlassImpfungErfasstId,
        @Nullable UUID anlassImpfungDurchgefuehrtId,
        @Nullable String aggregatedDiseases // this should have the form "HEPATITIS_B:true;MASERN:false"
    ) {
        this(
            id,
            untersuchungId,
            untersuchungImpfungenId,
            impfstoffId,
            chargeId,
            datumImpfung,
            impfStatus,
            anlassImpfungErfasstId,
            anlassImpfungDurchgefuehrtId,
            parseDiseases(aggregatedDiseases)
        );
    }

    @NonNull
    private static List<HelloDataImpfungInternKrankheitImmunitaetJson> parseDiseases(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(";"))
            .filter(entry -> !entry.isBlank())
            .map(entry -> {
                String[] parts = entry.split(":", 2);
                Krankheit krankheit = Krankheit.valueOf(parts[0]);
                boolean immunitaetGenuegend = parts.length > 1 && Boolean.parseBoolean(parts[1]);
                return new HelloDataImpfungInternKrankheitImmunitaetJson(krankheit, immunitaetGenuegend);
            })
            .toList();
    }
}
