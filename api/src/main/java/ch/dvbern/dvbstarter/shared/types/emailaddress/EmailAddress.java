package ch.dvbern.dvbstarter.shared.types.emailaddress;

import java.io.Serializable;
import java.util.Locale;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Format;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage.invalidEmail;
import static java.util.Objects.requireNonNull;

@Getter
@Schema(type = SchemaType.STRING, format = Format.EMAIL)
@Slf4j
public class EmailAddress implements Serializable {

    private static final long serialVersionUID = -2872872456003785391L;

    private final String address;

    protected EmailAddress(String address) {
        this.address = address;
    }

    public static EmailAddress fromString(String s) {
        requireNonNull(s);

        return parse(s);
    }

    static EmailAddress parse(String input) {
        // Locale.ROOT: with e.g. a Turkish default locale, toLowerCase() would turn 'I' into a dotless 'ı'
        String normalized = StringUtils.normalizeSpace(input)
            .toLowerCase(Locale.ROOT);
        if (!EmailValidator.getInstance(true).isValid(normalized)) {
            LOG.warn("Invalid email address: {}", input);
            throw new AppValidationException(invalidEmail(input));
        }

        return new EmailAddress(normalized);
    }

    /**
     * The same address in lowercase, for addresses that identify someone, e.g. the Benutzer matched at login.
     */
    @NonNull
    public EmailAddress toLowerCase() {
        return new EmailAddress(address.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailAddress that)) {
            return false;
        }

        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
