package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@ApplicationScoped
@Slf4j
class FileStoreDiffWhitelistLoader {

    /**
     * reads the whiltelist File and returns a set of whiltlisted ids, each ignored id should be on ints own line
     * @param whitelistFile filepath
     * @return Set of whitelisted ids
     */
    Set<String> load(@Nullable Path whitelistFile) {
        if (whitelistFile == null) {
            return Set.of();
        }
        try (Stream<String> lines = Files.lines(whitelistFile)) {
            return lines
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            LOG.warn(
                "ESC-FILESTORE-HEALTHCHECK: could not read whitelist file {}: {}",
                whitelistFile,
                e.getMessage());
            return Set.of();
        }
    }
}
