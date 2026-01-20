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
public class AnlassWithDateTimeJson {
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
    public AnlassWithDateTimeJson(
        @NonNull UUID anlassUUID,
        @NonNull AnlassTyp anlassTyp,
        @NonNull String titel,
        @NonNull LocalDate datum,
        @Nullable LocalDateTime zeitVon,
        @Nullable  LocalDateTime zeitBis
    ) {
        this.anlassId =  ID.of(anlassUUID, Anlass.class);
        this.anlassTyp =  anlassTyp;
        this.titel =  titel;
        this.datum = datum;
        this.zeitVon = zeitVon;
        this.zeitBis = zeitBis;
    }
}
