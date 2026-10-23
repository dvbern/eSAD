package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HelloDataImpfstatusKrankheitJson {

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

    @NotNull
    @NonNull
    @Schema(required = true)
    Krankheit krankheit;

    @Nullable
    @Schema(required = false)
    Integer anzahlImpfungen;

    @Nullable
    @Schema(required = false)
    LocalDate datumLetzteImpfung;

    @NotNull
    @NonNull
    @Schema(required = true)
    ImpfschutzGegeben impfschutzGegeben;

    @Schema(required = true)
    boolean impfempfehlung;

    @Schema(required = true)
    boolean impfentscheid;

    public HelloDataImpfstatusKrankheitJson(
        @NonNull UUID id,
        @NonNull UUID untersuchungId,
        @NonNull UUID untersuchungImpfungenId,
        @NonNull Krankheit krankheit,
        @Nullable Integer anzahlImpfungen,
        @Nullable LocalDate datumLetzteImpfung,
        @NonNull ImpfschutzGegeben impfschutzGegeben,
        @Nullable Boolean impfempfehlung,
        @Nullable Boolean impfentscheid
    ) {
        this(
            Objects.requireNonNull(id),
            Objects.requireNonNull(untersuchungId),
            Objects.requireNonNull(untersuchungImpfungenId),
            Objects.requireNonNull(krankheit),
            anzahlImpfungen,
            datumLetzteImpfung,
            Objects.requireNonNull(impfschutzGegeben),
            Boolean.TRUE.equals(impfempfehlung),
            Boolean.TRUE.equals(impfentscheid)
        );
    }
}
