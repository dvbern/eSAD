package ch.dvbern.oss.hemed.esc.api.features.benutzer.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Minimal-DTO eines Benutzers für Listen-/Tabellen-Ansichten:
 * Enthält nur die für Anzeige (Name), Sortierung und Filter (Id) benötigten Felder.
 */
@JaxDTO
@Value
@AllArgsConstructor
public class BenutzerNameJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    ID<Benutzer> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    String fullName;
}
