package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class ExampleDateTimeJson {

    @NonNull
    @Schema(required = true)
    private final LocalDate localDate;

    @NonNull
    @Schema(required = true)
    private final LocalDateTime localDateTime;

    @NonNull
    @Schema(required = true)
    private final OffsetDateTime offsetDateTime;

    @NonNull
    @Schema(required = true)
    private final ZonedDateTime zonedDateTime;

}
