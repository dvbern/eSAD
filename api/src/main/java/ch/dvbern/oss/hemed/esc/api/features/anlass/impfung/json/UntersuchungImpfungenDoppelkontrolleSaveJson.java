package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import jakarta.validation.Valid;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenDoppelkontrolleSaveJson {

    @Valid
    @Nullable
    @Schema(required = false)
    private BenutzerOptionJson visumPerson1;

    @Nullable
    @Schema(required = false)
    private LocalDate visumDatum1;

    @Valid
    @Nullable
    @Schema(required = false)
    private BenutzerOptionJson visumPerson2;

    @Nullable
    @Schema(required = false)
    private LocalDate visumDatum2;

}
