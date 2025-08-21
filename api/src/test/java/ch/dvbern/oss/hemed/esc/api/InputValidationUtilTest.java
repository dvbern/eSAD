package ch.dvbern.oss.hemed.esc.api;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.inputvalidation.InputValidationUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class InputValidationUtilTest {

    private static String charDebug(char character) {
        return String.format("Character: %s=0x%02X", Character.getName(character), (int) character);
    }

    @Nested
    class RemoveAllControlCharactersTest {

        static Set<Character> controlCharacters() {
            // See e.g.: http://jkorpela.fi/chars/c0.html
            // 0-31 + 127
            return Stream.concat(
                IntStream.rangeClosed(0, 31).mapToObj(charCode -> (char) charCode),
                Stream.of((char) 127)
            ).collect(toSet());
        }

        @ParameterizedTest
        @MethodSource("controlCharacters")
        void shouldReplaceAllControlCharacters(char controlCharacter) {
            String actual = InputValidationUtil.removeAllControlCharacters(String.valueOf(controlCharacter));

            assertThat(actual)
                .describedAs(charDebug(controlCharacter))
                .isEmpty();
        }

        @ParameterizedTest
        @MethodSource("controlCharacters")
        void shouldHandleMultipleCharacters(char controlCharacter) {
            //noinspection StringConcatenationMissingWhitespace
            String input = "a" + controlCharacter + 'b' + controlCharacter + 'c';
            String actual = InputValidationUtil.removeAllControlCharacters(input);

            assertThat(actual)
                .describedAs(charDebug(controlCharacter))
                .isEqualTo("abc");
        }

        @Test
        void shouldLeaveNormalCharacterIntact() {
            String actual = InputValidationUtil.removeAllControlCharacters("Hello World!");

            assertThat(actual)
                .isEqualTo("Hello World!");
        }
    }

    @Nested
    @SuppressWarnings("FieldNamingConvention")
    class RemoveUnprintableControlCharactersTest {
        private static final String TAB = "\t"; // 0x09
        private static final String LF = "\n";  // 0x0A
        private static final String CR = "\r";  // 0x0C

        static Set<Character> controlCharactersExceptPrintable() {
            // See e.g.: http://jkorpela.fi/chars/c0.html
            return Stream.concat(
                    Stream.concat(
                        Stream.concat(
                            Stream.concat(
                                IntStream.rangeClosed(0, 8).mapToObj(charCode -> (char) charCode),
                                Stream.of((char) 11)
                            ),
                            Stream.of((char) 12)
                        ),
                        IntStream.rangeClosed(14, 31).mapToObj(charCode -> (char) charCode)
                    ),
                    Stream.of((char) 127)
                )
                .collect(toSet());
        }

        @ParameterizedTest
        @MethodSource("controlCharactersExceptPrintable")
        void shouldReplaceUnprintables(char controlCharacter) {
            String actual = InputValidationUtil.removeUnprintables(String.valueOf(controlCharacter));

            assertThat(actual)
                .describedAs(charDebug(controlCharacter))
                .isEmpty();
        }

        @ParameterizedTest
        @MethodSource("controlCharactersExceptPrintable")
        void shouldHandleMultipleCharacters(char controlCharacter) {
            //noinspection StringConcatenationMissingWhitespace
            String input = "a" + controlCharacter + 'b' + controlCharacter + 'c';
            String actual = InputValidationUtil.removeUnprintables(input);

            assertThat(actual)
                .describedAs(charDebug(controlCharacter))
                .isEqualTo("abc");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            TAB,
            LF,
            CR,
        })
        void shoulKeepPrintablesIntact(String controlChar) {
            assertThat(InputValidationUtil.removeUnprintables(controlChar))
                .isEqualTo(controlChar);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            TAB,
            LF,
            CR,
        })
        void shouldKeepMultiplePrintablesIntact(String controlChar) {
            String input = 'a' + controlChar + 'b' + controlChar + 'c';

            assertThat(InputValidationUtil.removeUnprintables(input))
                .isEqualTo(input);
        }

        @Test
        void shouldLeaveNormalCharacterIntact() {
            String actual = InputValidationUtil.removeUnprintables("Hello World!");

            assertThat(actual)
                .isEqualTo("Hello World!");
        }

    }
}
