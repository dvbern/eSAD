package ch.dvbern.dvbstarter.shared.types.cleanfilename;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CleanFileNameTest {

    // we depend on InputValidationUtils for stripping of illegal characters

    @Nested
    class ParseString {
        @Test
        void parses_filename() {
            var actual = CleanFileName.parse("file.txt");

            assertThat(actual)
                .isEqualTo(new CleanFileName("file.txt"));
        }

        @Test
        void strips_path_linux_mac() {
            var actual = CleanFileName.parse("foo/bar/file.txt");

            assertThat(actual.getFileName())
                .isEqualTo("file.txt");
        }

        @Test
        void strips_path_windows() {
            var actual = CleanFileName.parse("c:\\foo\\bar\\file.txt");

            assertThat(actual.getFileName())
                .isEqualTo("file.txt");
        }
    }

    @Nested
    class ParsePath {
        @Test
        void parses_filename() {
            var actual = CleanFileName.parse(Path.of("file.txt"));

            assertThat(actual)
                .isEqualTo(new CleanFileName("file.txt"));
        }

        @Test
        void strips_path_linux() {
            var actual = CleanFileName.parse(Path.of("foo/bar/file.txt"));

            assertThat(actual.getFileName())
                .isEqualTo("file.txt");
        }

        @Test
        void strips_path_windows() {
            var actual = CleanFileName.parse(Path.of("c:\\foo\\bar\\file.txt"));

            assertThat(actual.getFileName())
                .isEqualTo("file.txt");
        }
    }

    @Test
    void toString_prints_the_clean_filename() {
        var input = CleanFileName.parse("foo/bar/file.txt");

        var actual = input.toString();

        assertThat(actual)
            .isEqualTo("file.txt");
    }

    @Test
    void cleanFileName_removes_windows_incompatible_characters() {
        var input = new CleanFileName("file<>\":|?*.txt");

        var actual = input.toString();

        assertThat(actual)
            .isEqualTo("file.txt");
    }
}
