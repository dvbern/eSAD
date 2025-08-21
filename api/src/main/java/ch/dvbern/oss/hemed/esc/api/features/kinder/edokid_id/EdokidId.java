package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Format;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Schema(type = SchemaType.STRING, format = Format.EDOKIDID, implementation = String.class)
@Slf4j
public class EdokidId implements Serializable {

    @Serial
    private static final long serialVersionUID = 2630683265288310456L;

    private static final int MINYEAR = 0;
    private static final int MAXYEAR = 99;
    private static final int NUMMERLENGTH = 3;
    private static final String REGEX_NUMMER = "\\d+";
    private static final String EDOKIDID_SEPERATOR = " ";
    private static final Pattern PATTERN = Pattern.compile(REGEX_NUMMER);

    private final char character;

    @NotNull
    private final String nummer;

    @NotNull
    private final String schulhausnummer;

    private final int eintrittsjahr;

    public EdokidId(char character, @NonNull String laufnummer, @NonNull String schulhausnummer, int eintrittsjahr) {

        this.eintrittsjahr = eintrittsjahr;
        this.character = character;
        this.nummer = laufnummer;
        this.schulhausnummer = schulhausnummer;
        if (!isValid()) {
            throw new AppValidationException(AppValidationMessage.invalidEdokidNummer(this.toString()));
        }
    }

    @Nullable
    public static EdokidId parse(@IgnoreForIdClassTest @Nullable String edokidId) {
        if (edokidId == null) {
            return null;
        }
        try {
            String normalized = StringUtils.normalizeSpace(edokidId);
            normalized = normalized.replace(" ", "");
            char character = normalized.charAt(0);
            String laufnummer = normalized.substring(1, 4);
            String schulhausnummer = normalized.substring(4, 7);
            int eintrittsjahr = Integer.parseInt(normalized.substring(7));
            return new EdokidId(character, laufnummer, schulhausnummer, eintrittsjahr);

        } catch (AppValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new AppValidationException(AppValidationMessage.invalidEdokidNummer(edokidId), e);
        }

    }

    private static boolean checkCharIsValid(char character) {
        return Character.isLetter(character);
    }

    private static boolean checkNummerIsCorrectLengthAndIsNumeric(String nummerToCheck) {
        return nummerToCheck.length() == NUMMERLENGTH && PATTERN.matcher(nummerToCheck).matches();
    }

    private static boolean checkIsInRange(int nummerToCheck) {
        return nummerToCheck >= MINYEAR && nummerToCheck <= MAXYEAR;
    }

    protected boolean isValid() {
        boolean characterValid = checkCharIsValid(character);
        boolean nummerValid = checkNummerIsCorrectLengthAndIsNumeric(nummer);
        boolean schulhausnummerValid = checkNummerIsCorrectLengthAndIsNumeric(schulhausnummer);
        boolean eintrittsjahrValid = checkIsInRange(eintrittsjahr);

        return characterValid && nummerValid && schulhausnummerValid && eintrittsjahrValid;
    }

    @NonNull
    @Override
    public String toString() {
        return character
            + nummer
            + schulhausnummer
            + eintrittsjahr;
    }

    @NonNull
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder(character + nummer + schulhausnummer + eintrittsjahr);
        sb.insert(2, EDOKIDID_SEPERATOR);
        sb.insert(5, EDOKIDID_SEPERATOR);
        sb.insert(9, EDOKIDID_SEPERATOR);
        return sb.toString();
    }

}
