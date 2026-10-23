package ch.dvbern.oss.hemed.esc.api.testutil.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jboss.logmanager.ExtFormatter;
import org.jboss.logmanager.ExtLogRecord;

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
 *         assertThat(handler.flatEntries())
 *             .containsExactly(
 *                 "INFO: full-load, id: 3fa85f64-5717-4562-b3fc-2c963f66afa6, timestamp: 2022-03-10T16:15:50Z, size: 3, schemaVersion: 1.2.3",
 *                 "INFO: update: 2022-03-10T16:15:50Z, person: 756.1111.1111.11, AN: [93f779c2-72ee-463b-9264-604a739cf297:2022-03-10-2022-03-10], FamZ: []"
 *             );
 *     }
 * }</pre>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryLogHandler extends Handler {

    /**
     * Handles all ExtLogRecord format styles (printf, MessageFormat, none) and falls back to JUL formatting for plain
     * LogRecords.
     */
    private static final ExtFormatter MESSAGE_FORMATTER = new ExtFormatter() {
        @Override
        public String format(ExtLogRecord logRecord) {
            return formatMessage(logRecord);
        }
    };

    // strong reference: the LogManager only holds loggers weakly, a collected logger would silently drop this handler
    private final Logger logger;

    // records may arrive from other threads, e.g. the transaction reaper
    @Getter
    @Accessors(fluent = true)
    private final List<LogRecord> entries = new CopyOnWriteArrayList<>();

    /**
     * Convenience: instantiate and {@link #install()}
     */
    public static InMemoryLogHandler installFor(Class<?> clazz) {
        return new InMemoryLogHandler(Logger.getLogger(clazz.getName())).install();
    }

    public InMemoryLogHandler install() {
        logger.addHandler(this);
        return this;
    }

    public void uninstall() {
        logger.removeHandler(this);
    }

    public List<String> flatEntries() {
        return entries.stream()
            .map(r -> "%s: %s".formatted(r.getLevel(), formattedMessage(r)))
            .toList();
    }

    public static String formattedMessage(LogRecord logRecord) {
        return MESSAGE_FORMATTER.formatMessage(logRecord);
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (logRecord instanceof ExtLogRecord extLogRecord) {
            // keep the MDC of the logging thread, otherwise it is read from the thread that inspects the record
            extLogRecord.copyMdc();
        }
        entries.add(logRecord);
    }

    @Override
    public void flush() {
        // nothing buffered
    }

    @Override
    public void close() {
        // nothing to release
    }
}
