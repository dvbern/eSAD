package ch.dvbern.dvbstarter.shared;

import java.time.LocalDate;
import java.time.Month;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidIdGeneratorService;
import jakarta.validation.constraints.NotEmpty;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Test Provider for EdokidId, it provides tries to
 * generate valid EdokidId for testing purposes.
 * Each Id should be unique. The Service will track the generated Id's
 * and return a new one each time it is called.
 * The Test-Generator is inspired by the SozialversicherungsNummerTestGenerator
 */
@UtilityClass
public class EdokidIdTestGenerator {

    public static final String EDOKID_ID_REGEX_PATTERN = "[A-Z][0-9] [0-9]{2} [0-9]{3} [0-9]{2}";
    public static final String EDOKID_ID_NO_SCHOOL_REGEX_PATTERN = "[A-Z][0-9] [0-9]{2} 000 [0-9]{2}";
    private final FixedClockProvider FIXED_CLOCK_PROVIDER = FixedClockProvider.now();
    private final AppClock APP_CLOCK = new AppClock(FIXED_CLOCK_PROVIDER);
    private static final Pattern PATTERN = Pattern.compile("\\d+");
    private static char character = 'A';
    private static int laufnummer = 0;

    @NonNull
    public static EdokidId getNextEdokidId(@Nullable String schulhausnummer) {
        if (laufnummer == 999) {
            laufnummer = 0;
            character++;
        } else {
            laufnummer++;
        }
        if (schulhausnummer == null || schulhausnummer.isEmpty()) {
            schulhausnummer = EdokidIdGeneratorService.MISSING_SCHULE_NUM;
        }
        return new EdokidId(
            character,
            getNummerWith3Digits(String.valueOf(laufnummer)),
            getNummerWith3Digits(schulhausnummer),
            getEintrittsJahr());
    }

    @NonNull
    private String getNummerWith3Digits(@NonNull @NotEmpty String nummer) {
        if (PATTERN.matcher(nummer).matches() && nummer.length() <= 3) {
            return String.format("%03d", Integer.parseInt(nummer)); // 3 digits
        }
        throw new AppValidationException(AppValidationMessage.invalidEdokidNummer(nummer));
    }

    private int getEintrittsJahr() {
        LocalDate localDate = APP_CLOCK.localDateTime().now().toLocalDate();

        int jahr = localDate.isBefore(LocalDate.of(localDate.getYear(), Month.AUGUST, 1)) ?
            localDate.getYear() - 1 :
            localDate.getYear();

        return Integer.parseInt(Integer.toString(jahr).substring(2));
    }
}
