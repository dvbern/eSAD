package ch.dvbern.oss.hemed.esc.api;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UtilTest {

    @ParameterizedTest
    @CsvSource(
        value = {
            "a b c|a,b,c",
            "\"a b c\"|a b c",
            "a \"b c\"|a,b c",
            "\"a b c|a b c",
            " too   much whitespace|,too,,,much,whitespace",
        },
        delimiter = '|',
        ignoreLeadingAndTrailingWhitespace = false
    )
    void tokenizeStringKeepingQuotedParts(String input, String expectedAsCsv) {
        List<String> output = Arrays.asList(expectedAsCsv.split(","));

        var tokens = Util.tokenizeStringKeepingQuotedParts(input, ' ', '"');

        Assertions.assertThat(tokens).containsExactlyInAnyOrderElementsOf(output);
    }
}
