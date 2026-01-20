package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public enum PupilKlasseStufe {

    KINDERGARTEN("Kindergarten"),
    PRIMARSCHULE("Primar"),
    SEKUNDARSTUFE("SEK"),
    SEKUNDARSTUFE_II("SEK II");

    private final String klassenStufeValue;

    @NonNull
    public static PupilKlasseStufe fromString(@NonNull String value) {
        for (PupilKlasseStufe stufe : PupilKlasseStufe.values()) {
            if (stufe.klassenStufeValue.equalsIgnoreCase(value)) {
                return stufe;
            }
        }
        throw new AppFailureException(AppFailureMessage.internalError(
            "Unmapped KlasseStufe from pupil: '%s'".formatted(value))
        );
    }
}
