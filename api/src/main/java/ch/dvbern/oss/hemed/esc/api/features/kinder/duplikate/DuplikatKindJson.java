package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class DuplikatKindJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private ID<Kind> id;

    @Nullable
    @Schema(required = false)
    private String name;

    @Nullable
    @Schema(required = false)
    private String vorname;

    @Nullable
    @Schema(required = false)
    private Geschlecht geschlecht;

    @Nullable
    @Schema(required = false)
    private LocalDate geburtsdatum;

    @Nullable
    @Schema(required = false)
    private LocalDateTime timestampErstellt;

    @Nullable
    @Schema(required = false)
    private final EntityAktivStatus aktivStatus;

    @Nullable
    @Schema(required = false)
    private OffsetDateTime timestampLatestImport;

    @NotNull
    @Schema(required = true)
    private final boolean escada;

    @Nullable
    @Schema(required = false)
    private String schule;

    @Nullable
    @Schema(required = false)
    private String klasse;
}
