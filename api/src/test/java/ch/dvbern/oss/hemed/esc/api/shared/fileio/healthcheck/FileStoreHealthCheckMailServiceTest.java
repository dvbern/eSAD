package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStoreHealthCheckMailServiceTest {

    private MailDAO mailDAO;
    private ConfigService configService;
    private AppClock appClock;
    private FileStoreHealthCheckMailService mailService;

    @BeforeEach
    void setUp() {
        mailDAO = mock(MailDAO.class);
        configService = mock(ConfigService.class);
        appClock = mock(AppClock.class);
        mailService = new FileStoreHealthCheckMailService(mailDAO, configService, appClock);

        AppClock.LocalDateTimeProvider ldt = mock(AppClock.LocalDateTimeProvider.class);
        when(appClock.localDateTime()).thenReturn(ldt);
        when(ldt.now()).thenReturn(LocalDateTime.of(2025, 1, 1, 3, 30));
        when(configService.getFilestoreHealthcheckReportMaxEntries()).thenReturn(500);
    }

    @Test
    void sendReport_noRecipients_doesNotSendMail() {
        // ARRANGE
        var report = new FileStoreDiffReport(Set.of("db-only"), Set.of(), 0, 0);

        // ACT
        mailService.sendReport(report, "BS", List.of());

        // ASSERT
        verify(mailDAO, never()).sendEmailSync(anyString(), anyString(), anyString());
    }

    @Test
    void sendReport_withRecipients_sendsMailToEachRecipient() throws IOException {
        // ARRANGE
        var report = new FileStoreDiffReport(Set.of("db-only-1"), Set.of("s3-only-1"), 0, 0);

        // ACT
        mailService.sendReport(report, "BS", List.of("admin1@test.ch", "admin2@test.ch"));

        // ASSERT
        verify(mailDAO, times(2)).sendEmailWithAttachmentSync(anyString(), anyString(), anyString(), any());
    }

    @Test
    void sendReport_subjectContainsCounts() throws IOException {
        // ARRANGE
        var report = new FileStoreDiffReport(Set.of("db-id"), Set.of("s3-id"), 0, 0);

        // ACT
        mailService.sendReport(report, "BE", List.of("admin@test.ch"));

        // ASSERT
        verify(mailDAO).sendEmailWithAttachmentSync(
            anyString(),
            contains("1 in DB only"),
            anyString(),
            any()
        );
    }

    @Test
    void sendReport_truncatesBodyAtMaxEntries() throws IOException {
        // ARRANGE
        when(configService.getFilestoreHealthcheckReportMaxEntries()).thenReturn(2);
        Set<String> bigDbOnly = Set.of("id-1", "id-2", "id-3");
        var report = new FileStoreDiffReport(bigDbOnly, Set.of(), 0, 0);

        // ACT
        mailService.sendReport(report, "WIN", List.of("admin@test.ch"));

        // ASSERT
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailDAO).sendEmailWithAttachmentSync(
            anyString(),
            anyString(),
            bodyCaptor.capture(),
            any()
        );
        assertThat(bodyCaptor.getValue())
            .contains("  id-1")
            .contains("  id-2")
            .doesNotContain("  id-3")
            .contains("+1 more");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendReport_attachmentContainsAllEntriesAndIgnoredCounts() throws IOException {
        // ARRANGE
        when(configService.getFilestoreHealthcheckReportMaxEntries()).thenReturn(2);
        var report = new FileStoreDiffReport(Set.of("id-3", "id-1", "id-2"), Set.of("s3-1"), 5, 4);

        // ACT
        mailService.sendReport(report, "BS", List.of("admin@test.ch"));

        // ASSERT
        ArgumentCaptor<List<Path>> captor = ArgumentCaptor.forClass(List.class);
        verify(mailDAO).sendEmailWithAttachmentSync(anyString(), anyString(), anyString(), captor.capture());
        String content = Files.readString(captor.getValue().getFirst());
        assertThat(content)
            .startsWith("FileStore drift report — tenant: BS, DB-only: 3 (ignored: 5), S3-only: 1 (ignored: 4)\n")
            .contains("id-1\nid-2\nid-3\n")
            .contains("s3-1\n");
    }

    @Test
    void sendReport_mailSendThrows_logsErrorWithoutPropagating() throws IOException {
        // ARRANGE
        var report = new FileStoreDiffReport(Set.of("id"), Set.of(), 0, 0);
        doThrow(new RuntimeException("Mailserver down"))
            .when(mailDAO).sendEmailWithAttachmentSync(anyString(), anyString(), anyString(), any());

        // ACT + ASSERT
        Assertions.assertThatNoException()
            .isThrownBy(() -> mailService.sendReport(report, "BS", List.of("admin@test.ch")));
    }
}
