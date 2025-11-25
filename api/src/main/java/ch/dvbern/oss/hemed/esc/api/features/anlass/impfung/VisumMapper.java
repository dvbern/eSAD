package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class VisumMapper {

    private final BenutzerService benutzerService;

    @Nullable
    public Visum toEntity(
        @Nullable Visum existingVisum,
        @Nullable BenutzerOptionJson visumBenutzerOrNull,
        @Nullable LocalDate visumDatumOrNull
    ) {
        Benutzer visumPerson = visumBenutzerOrNull != null ? benutzerService.getById(visumBenutzerOrNull.getId()) : null;
        // Neu nicht mehr vorhanden da keine Person geschickt wurde
        if (visumPerson == null) {
            return null;
        }
        // Vorher und nachher vorhanden
        if (existingVisum != null) {
            existingVisum.setPerson(visumPerson);
            existingVisum.setDatumVisiert(visumDatumOrNull);
            return existingVisum;
        }
        // Neu vorhanden
        return new Visum(visumPerson, visumDatumOrNull);
    }
}
