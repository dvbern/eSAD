package ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Format;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage.invalidSozialversicherungsnummer;

@Getter
@Schema(type = SchemaType.STRING, format = Format.SOZIALVERSICHERUNGSNUMMER, implementation = String.class)
public class SozialversicherungsNummer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1879351341841790754L;

    private static final long MAX_VALUE = 9999999999999L;
    private static final long MIN_VALUE = 1000000000000L;
    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private final long nummer;
    private final int pruefziffer;

    public SozialversicherungsNummer(@Nonnull final String nummer) {
        this(Long.parseLong(NON_DIGIT.matcher(nummer).replaceAll("")));
    }

    public SozialversicherungsNummer(final long nummer) {
        checkRange(nummer);
        this.nummer = nummer;
        this.pruefziffer = berechnePruefziffer(nummer);
        if (!isValid()) {
            throw new AppValidationException(invalidSozialversicherungsnummer(toString()));
        }
    }

    private void checkRange(final long nummerToCheck) {
        Long value = nummerToCheck;
        boolean valid = isInRange(value);
        if (!valid) {
            throw new IllegalArgumentException("Argument " + nummerToCheck + " not in range");
        }
    }

    private static boolean isInRange(@Nonnull Long value) {
        if (value.compareTo(MIN_VALUE) < 0) {
            return false;
        }
        return value.compareTo(MAX_VALUE) <= 0;
    }

    static int berechnePruefziffer(final long nummerToCalculate) {
        // Entferne die Pruefziffer (ignoriere sie für die Pruefziffer Berechnung)
        long n = nummerToCalculate / 10;
        int currentPruefziffer = 0;
        int z = 1;
        while (n > 0) {
            long ziffer = n % 10;
            currentPruefziffer = incrementPruefziffer(currentPruefziffer, (int) ziffer, z);
            z++;
            n /= 10;
        }
        int returnValue = 0;
        if (currentPruefziffer % 10 != 0) {
            returnValue = 10 - currentPruefziffer % 10;
        }
        return returnValue;
    }

    static int incrementPruefziffer(final int currentPruefziffer, final int ziffer, final int z) {
        int increment = ziffer;
        if (z % 2 != 0) {
            increment *= 3;
        }
        return currentPruefziffer + increment;
    }

    @Nonnull
    @Override
    public String toString() {
        return toString(nummer);
    }

    @Nonnull
    private static String toString(long nummer) {
        return Long.toString(nummer);
    }

    @Nonnull
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder(Long.toString(nummer));
        sb.insert(3, '.');
        sb.insert(8, '.');
        sb.insert(13, '.');
        return sb.toString();
    }

    public boolean isValid() {
        return isValidImpl(nummer, pruefziffer);
    }

    public static boolean isValidImpl(long nummer, int pruefziffer) {
        return nummer % 10 == pruefziffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SozialversicherungsNummer that = (SozialversicherungsNummer) o;
        return nummer == that.nummer && pruefziffer == that.pruefziffer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nummer, pruefziffer);
    }
}
