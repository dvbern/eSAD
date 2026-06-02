package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class FileStoreDiffWhitelistLoaderTest {

    private final FileStoreDiffWhitelistLoader loader = new FileStoreDiffWhitelistLoader();

    @Test
    void load_nullPath_returnsEmptySet() {
        // ACT + ASSERT
        assertThat(loader.load(null)).isEmpty();
    }

    @Test
    void load_nonExistentFile_returnsEmptySetWithoutThrowing(@TempDir Path tempDir) {
        // ARRANGE
        Path missing = tempDir.resolve("does-not-exist.txt");

        // ACT + ASSERT
        assertThat(loader.load(missing)).isEmpty();
    }

    @Test
    void load_validFile_parsesEntriesAndIgnoresCommentsAndBlankLines(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path whitelist = tempDir.resolve("whitelist.txt");
        Files.writeString(whitelist, """
            # this is a comment
            file-id-aaa
            file-id-bbb

            # another comment
              file-id-ccc  \s
            """);

        // ACT
        Set<String> result = loader.load(whitelist);

        // ASSERT
        assertThat(result).containsExactlyInAnyOrder("file-id-aaa", "file-id-bbb", "file-id-ccc");
    }

    @Test
    void load_emptyFile_returnsEmptySet(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path empty = tempDir.resolve("empty.txt");
        Files.writeString(empty, "");

        // ACT + ASSERT
        assertThat(loader.load(empty)).isEmpty();
    }

    @Test
    void load_onlyCommentsAndBlanks_returnsEmptySet(@TempDir Path tempDir) throws IOException {
        // ARRANGE
        Path onlyComments = tempDir.resolve("comments.txt");
        Files.writeString(onlyComments, "# comment\n\n# another\n");

        // ACT + ASSERT
        assertThat(loader.load(onlyComments)).isEmpty();
    }
}
