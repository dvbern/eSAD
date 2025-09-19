package ch.dvbern.oss.hemed.esc.api.shared.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringNormalizerUtilTest {

    @ParameterizedTest
    @CsvSource({
        "'éèêëäää', 'eeeeaeaeae'",
        "'résūmé', 'resume'",
        "'hello world', 'hello world'",
        "'', ''",
        "'ÉÈÊËÄÖÜ', 'eeeeaeoeue'",
        "'çñšž', 'cnsz'",
        "'ÁáÀàÂâÃã', 'aaaaaaaa'",
        "'ČčĆć', 'cccc'",
        "'Îïíîì', 'iiiii'",
        "'Ûûüùú', 'uuueuu'",
        "'aeiouAEIOU', 'aeiouaeiou'",
        "'!@# $%^&*()_+', '!@# $%^&*()_+'",
        "'1234567890', '1234567890'"
    })
    void testNormalizeAndStripDiarcritics(@NonNull String input, @NonNull String expected) {
        String result = StringNormalizerUtil.normalizeAndStripDiarcritics(input);
        assertThat(result).isEqualTo(expected);
    }
}
