package ch.dvbern.dvbstarter.shared.types.phonenumber;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NormalizedPhoneNumberTest {

    @Test
    void empty_string_is_not_valid() {
        assertThrows(AppValidationException.class, () -> new NormalizedPhoneNumber(""));
    }

    @Test
    void parse_null_returns_null() {
        assertNull(NormalizedPhoneNumber.parse(null));
    }

    @Test
    void blank_string_is_not_valid() {
        assertThrows(AppValidationException.class, () -> new NormalizedPhoneNumber("  "));
    }

    @ParameterizedTest
    @CsvSource({
        "+41791231212     , +41791231212 , +41 79 123 12 12",
        "+41 79 123 12 12 , +41791231212 , +41 79 123 12 12",
        "0791231212       , +41791231212 , +41 79 123 12 12",
        "79 123 12 12     , +41791231212 , +41 79 123 12 12",
        "034   12345   67 , +41341234567 , +41 34 123 45 67",
        "+41 75 123 12 12 , +41751231212 , +41 75 123 12 12",
        "781231212        , +41781231212 , +41 78 123 12 12",
        "0781231212       , +41781231212 , +41 78 123 12 12",
        "+41781231212     , +41781231212 , +41 78 123 12 12",
        "0041781231212    , +41781231212 , +41 78 123 12 12",
        "  078 123 1212   , +41781231212 , +41 78 123 12 12",
        "  078 123 12 12  , +41781231212 , +41 78 123 12 12",

    })
    void should_normalize_and_format_correctly(
        @NonNull String input,
        @NonNull String expectedToString,
        @NonNull String expectedFormattedPhoneNumber
    ) {
        NormalizedPhoneNumber normalizedPhoneNumber = new NormalizedPhoneNumber(input);
        assertEquals(expectedToString, normalizedPhoneNumber.toString());
        assertEquals(expectedFormattedPhoneNumber, normalizedPhoneNumber.getFormattedPhoneNumber());
    }

    @ParameterizedTest
    @CsvSource({
        "  +41 78 123q12 12   , true  , +41781231212 , +41 78 123 12 12",
        "  +41 78 123q12 12w  , true  , +41781231212 , +41 78 123 12 12",
        "  +41 78t 123q12 12  , true  , +41781231212 , +41 78 123 12 12",
        "  +41 78x 123x 12 12 , true  , +41781231212 , +41 78 123 12 12",
        "  +41 78 123 zzzz    , true  , +41781239999 , +41 78 123 99 99",
        "  +41 78  123q12 fd  , false ,              , ",
        "  +41 78  123456 fd  , false ,              , ",
        "  +41 78  1234ab cd  , false ,              , ",
        "  +41 78  123q12 fd  , false ,              , ",
    })
    void should_ignore_some_mistyped_digits(
        @NonNull String input,
        boolean isValid,
        @Nullable String expectedToString,
        @Nullable String expectedFormattedPhoneNumber
    ) {
        if (isValid) {
            NormalizedPhoneNumber normalizedPhoneNumber = new NormalizedPhoneNumber(input);
            assertEquals(expectedToString, normalizedPhoneNumber.toString());
            assertEquals(expectedFormattedPhoneNumber, normalizedPhoneNumber.getFormattedPhoneNumber());
        } else {
            assertThrows(AppValidationException.class, () -> NormalizedPhoneNumber.parse(input));
        }
    }

    @Test
    void allow_vanity_numbers() {
        // googles libphonenumber is pretty lenient and allows vanity numbers, characters  are converter if ocuring
        // at the end of the number
        NormalizedPhoneNumber normalizedPhoneNumber = NormalizedPhoneNumber.parse("034 402 ab cd");
        assertEquals("+41344022223", normalizedPhoneNumber.toString());
        assertEquals("+41 34 402 22 23", normalizedPhoneNumber.getFormattedPhoneNumber());
    }

    @Test
    void disallow_ermegency_numbers() {
        // googles libphonenumber would require us to use ShortNumberInfo to check for emergency numbers, we do not
        // want to allow them in our system
        assertThrows(AppValidationException.class, () -> NormalizedPhoneNumber.parse("118"));
        assertThrows(AppValidationException.class, () -> NormalizedPhoneNumber.parse("117"));
        assertThrows(AppValidationException.class, () -> NormalizedPhoneNumber.parse("1414"));
    }

    @ParameterizedTest
    @CsvSource({
        "+41 75 123 12 12, true",
        "+41 76 123 12 12, true",
        "+41 77 123 12 12, true",
        "+41 78 123 12 12, true",
        "+41 79 123 12 12, true",
        "+41 34 402 45 12, false",
        "034 402 45 12, false"
    })
    void should_identify_mobile_numbers_correctly(
        @NonNull String input,
        boolean isMobileNumber
    ) {
        NormalizedPhoneNumber normalizedPhoneNumber = NormalizedPhoneNumber.parse(input);
        assertEquals(isMobileNumber, normalizedPhoneNumber.isMobileNumber());
    }

}
