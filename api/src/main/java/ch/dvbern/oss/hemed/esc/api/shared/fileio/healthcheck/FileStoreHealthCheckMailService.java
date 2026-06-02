package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
class FileStoreHealthCheckMailService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm", Locale.GERMAN);
    private static final String BR = "<br>\n";

    private final MailDAO mailDAO;
    private final ConfigService configService;
    private final AppClock appClock;

    void sendReport(
        @NonNull FileStoreDiffReport report,
        @NonNull String tenantLabel,
        @NonNull List<String> recipients
    ) {
        if (recipients.isEmpty()) {
            LOG.warn("ESC-FILESTORE-HEALTHCHECK[{}]: no admin recipients configured, skipping mail", tenantLabel);
            return;
        }
        int max = configService.getFilestoreHealthcheckReportMaxEntries();
        String subject = "[eSAD S3 Healthcheck] FileStore drift: %d in DB only, %d in S3 only"
            .formatted(report.dbOnlyTotal(), report.s3OnlyTotal());

        List<Path> attachments = List.of();
        try {
            attachments = List.of(buildAttachment(report, tenantLabel));
        } catch (IOException e) {
            LOG.error("ESC-FILESTORE-HEALTHCHECK[{}]: could not create attachment, sending without", tenantLabel, e);
        }

        String emailBody = buildBody(report, tenantLabel, max, !attachments.isEmpty());
        for (String recipient : recipients) {
            try {
                if (attachments.isEmpty()) {
                    mailDAO.sendEmailSync(recipient, subject, emailBody);
                } else {
                    mailDAO.sendEmailWithAttachmentSync(recipient, subject, emailBody, attachments);
                }
                LOG.info("ESC-FILESTORE-HEALTHCHECK[{}]: drift report sent to {}", tenantLabel, recipient);
            } catch (Exception e) {
                LOG.error("ESC-FILESTORE-HEALTHCHECK[{}]: failed to send mail to {}", tenantLabel, recipient, e);
            }
        }
    }

    @NonNull
    private String buildBody(@NonNull FileStoreDiffReport report, @NonNull String tenantLabel, int max, boolean hasAttachment) {
        StringBuilder sb = new StringBuilder("<pre>");
        sb.append("Dear ESC Admin,").append(BR).append(BR);
        sb.append("The nightly file store health check found drift for tenant <b>")
            .append(tenantLabel).append("</b>:").append(BR).append(BR);
        sb.append("In DB only (referenced but missing in S3):   ").append(report.dbOnlyTotal()).append(BR);
        sb.append("In S3 only (orphaned references to s3 in db):").append(report.s3OnlyTotal()).append(BR).append(BR);
        appendSection(sb, "In DB only", report.dbOnly(), report.dbOnlyTotal(), max);
        appendSection(sb, "In S3 only", report.s3Only(), report.s3OnlyTotal(), max);
        if (hasAttachment) {
            sb.append("The full diff is attached.").append(BR);
        }
        sb.append("</pre>");
        return sb.toString();
    }

    private void appendSection(@NonNull StringBuilder sb, @NonNull String title, @NonNull Set<String> items, int total, int max) {
        sb.append(title)
            .append(" (showing ")
            .append(Math.min(total, max))
            .append(" of ")
            .append(total)
            .append("):").append(BR);
        items.stream().limit(max).sorted().forEach(id -> sb.append("  ").append(id).append(BR));
        if (total > max) {
            sb.append("  ... +").append(total - max).append(" more").append(BR);
        }
        sb.append(BR);
    }

    @NonNull
    private Path buildAttachment(@NonNull FileStoreDiffReport report, @NonNull String tenantLabel) throws IOException {
        String ts = appClock.localDateTime().now().format(TIMESTAMP_FORMAT);
        String filename = "filestore-drift-%s-%s".formatted(tenantLabel.toLowerCase(Locale.ROOT), ts);
        Path file = Files.createTempFile(filename, ".txt");
        file.toFile().deleteOnExit();

        StringBuilder sb = new StringBuilder();
        sb.append("FileStore drift report — tenant: ").append(tenantLabel)
            .append(", DB-only: ").append(report.dbOnlyTotal())
            .append(", S3-only: ").append(report.s3OnlyTotal()).append("\n\n");
        sb.append("=== IN DB ONLY ===\n");
        report.dbOnly().stream().sorted().forEach(id -> sb.append(id).append('\n'));
        sb.append("\n=== IN S3 ONLY ===\n");
        report.s3Only().stream().sorted().forEach(id -> sb.append(id).append('\n'));
        Files.writeString(file, sb.toString());
        return file;
    }
}
