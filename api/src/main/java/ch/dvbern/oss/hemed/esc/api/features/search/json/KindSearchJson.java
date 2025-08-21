package ch.dvbern.oss.hemed.esc.api.features.search.json;

import java.time.LocalDate;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class KindSearchJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    ID<Kind> id;

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    String esadId;

    @Nullable
    @Schema(required = false)
    SozialversicherungsNummer sozialversicherungsNummer;

    @NotNull
    @NonNull
    @Schema(required = true)
    String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    LocalDate geburtsdatum;

    @QueryProjection
    public KindSearchJson(
        @NonNull UUID id,
        @Nullable String esadId,
        @Nullable SozialversicherungsNummer sozialversicherungsNummer,
        @NonNull String name,
        @NonNull String vorname,
        @NonNull Geschlecht geschlecht,
        @NonNull LocalDate geburtsdatum
    ) {
        this.id = ID.of(id, Kind.class);
        this.esadId = esadId;
        this.sozialversicherungsNummer = sozialversicherungsNummer;
        this.name = name;
        this.vorname = vorname;
        this.geschlecht = geschlecht;
        this.geburtsdatum = geburtsdatum;
    }
}
