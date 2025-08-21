package ch.dvbern.oss.hemed.esc.api.testutil.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Allow for testing log output.
 * <p>
 * Usage:
 * <pre>{@code
 *     private final InMemoryLogHandler handler = InMemoryLogHandler.installFor(PersonLoadResource.class);
 *
 *     @AfterEach
 *     void afterEach() {
 *         handler.uninstall();
 *     }
 *
 *     @Test
 *     void verifyFoo() {
 *          assertThat(handler.flatEntries())
 *              .containsExactly(
 *                "INFO: full-load, id: 3fa85f64-5717-4562-b3fc-2c963f66afa6, timestamp: 2022-03-10T16:15:50Z, size: 3, schemaVersion: 1.2.3",
 *                "INFO: update: 2022-03-10T16:15:50Z, person: 756.1111.1111.11, AN: [93f779c2-72ee-463b-9264-604a739cf297:2022-03-10-2022-03-10], FamZ: []",
 *                "INFO: update: 2022-03-10T16:15:50Z, person: 756.2222.2222.22, AN: [93f779c2-72ee-463b-9264-604a739cf297:2022-03-10-2022-03-10], FamZ: [93f779c2-72ee-463b-9264-604a739cf297:2022-03-10-2022-03-10]",
 *                "INFO: update: 2022-03-10T16:15:50Z, person: 756.3333.3333.33, AN: [1757dc20-2734-47c3-9415-57e78bbd856d:2005-01-01-2010-12-31, becf6a89-2971-43c5-9008-a9f1d23198be:2011-01-01-null], FamZ: [a2c7cb2f-e9bc-4b74-9d6e-249582f26797:2000-01-01-2010-12-31]"
 *         );
 *     }
 * }</pre>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryLogHandler extends Handler {
    private final String loggerName;

    @Getter
    @Accessors(fluent = true)
    private final List<LogRecord> entries = new ArrayList<>();

    /**
     * Convenience: instantiate and {@link #install()}
     */
    public static InMemoryLogHandler installFor(Class<?> clazz) {
        var loggerName = clazz.getName();
        var handler = new InMemoryLogHandler(loggerName);

        return handler.install();
    }

    public InMemoryLogHandler install() {
        LogManager.getLogManager()
            .getLogger(loggerName)
            .addHandler(this);

        return this;
    }

    public void uninstall() {
        LogManager.getLogManager()
            .getLogger(loggerName)
            .removeHandler(this);
    }

    public List<String> flatEntries() {
        List<String> result = entries.stream()
            .map(r -> "%s: %s".formatted(r.getLevel(), r.getMessage()))
            .toList();

        return result;
    }

    @Override
    public void publish(LogRecord record) {
        entries.add(record);
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public void close() throws SecurityException {
        //
    }
}
