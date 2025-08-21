package ch.dvbern.oss.hemed.esc.api.shared.mail.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import io.quarkus.mailer.Attachment;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.from;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class MailDAOTest {

    private final MailDAO mailDAO;
    private final MockMailbox mailbox;
    private final ConfigService configService;

    private static final String TO_ADDRESS = "target@example.com";
    private static final String SUBJECT = "have a look";
    private static final String HTML_CONTENT = "my-html-content";
    private static final String ATTACHMENT_NAME = "sample-attachment.txt";
    private static final Duration AWAIT_DURATION = Duration.ofSeconds(5);

    private String subjecPrefix = "";
    private String isNotProdHint = "";
    private String expectedHtmlContent = "";
    private String expectedSubject = "";
    private String expectedRecipient = TO_ADDRESS;
    private int expectedSize = 1;
    private Path tempAttachment = null;

    @BeforeEach
    void setUp() {
        tempAttachment = TestUtil.createTempFileFor(
            Path.of("MailDaoTest", ATTACHMENT_NAME),
            ATTACHMENT_NAME
        );
        mailbox.clear();
        if (!configService.isStufeProd()) {
            subjecPrefix =
                String.format("eSAD%s: ", String.format(" (%s)", configService.getEnvironment()));
            isNotProdHint = "<br><br>Diese Nachricht wurde aus einem Testsystem versendet.";
        }
        if (configService.isMailOverridingRecipientsEnabled()) {
            isNotProdHint = isNotProdHint + "<br><br>Sie erhalten diese Nachricht anstelle von to = "
                + TO_ADDRESS + ", cc =  und bcc = , weil ein Empfänger eingetragen ist, an den alle E-Mails geschickt "
                + "werden müssen.";
            var optionalOverridingAll = configService.getMailOverridingRecipientsAddress();
            expectedRecipient = String.join(", ", optionalOverridingAll);
            expectedSize = 3;
        }
        expectedHtmlContent = HTML_CONTENT + isNotProdHint;
        expectedSubject = subjecPrefix + SUBJECT;
    }

    @Test
    void send_mail_without_attachments() {
        assertThatCode(() -> {
            var sendEmail = mailDAO.sendEmail(TO_ADDRESS, SUBJECT, HTML_CONTENT);

            sendEmail
                .await()
                .atMost(AWAIT_DURATION);
        }).doesNotThrowAnyException();

        assertMail(
            expectedRecipient,
            expectedSubject,
            expectedHtmlContent,
            null,
            expectedSize
        );
    }

    @Test
    void send_mail_sync_without_attachments() {
        assertThatCode(() -> mailDAO.sendEmailSync(
            TO_ADDRESS,
            SUBJECT,
            HTML_CONTENT)).doesNotThrowAnyException();

        assertMail(
            expectedRecipient,
            expectedSubject,
            expectedHtmlContent,
            null,
            expectedSize
        );

    }

    @Test
    void send_mail_with_attachments() throws IOException {
        var sendEmail =
            mailDAO.sendEmailWithAttachment(TO_ADDRESS, SUBJECT, HTML_CONTENT, List.of(tempAttachment));
        sendEmail
            .await()
            .atMost(AWAIT_DURATION);

        assertMail(
            expectedRecipient,
            expectedSubject,
            expectedHtmlContent,
            List.of(tempAttachment.getFileName().toString()),
            expectedSize
        );
    }

    @Test
    void send_mail_sync_with_attachments() throws IOException {
        mailDAO.sendEmailWithAttachmentSync(TO_ADDRESS, SUBJECT, HTML_CONTENT, List.of(tempAttachment));

        assertMail(
            TO_ADDRESS,
            expectedSubject,
            expectedHtmlContent,
            List.of(tempAttachment.getFileName().toString()),
            expectedSize
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void assertMail(
        @NonNull String to,
        @NonNull String subject,
        @NonNull String htmlContent,
        @Nullable List<String> attachmentNames,
        int expectedSize
    ) {
        List<Mail> mailsSentTo = mailbox.getMailsSentTo(to);
        assertThat(mailsSentTo)
            .hasSize(expectedSize)
            .allSatisfy(mail -> {
                assertThat(mail)
                    .returns(subject, from(Mail::getSubject))
                    .returns(htmlContent, from(Mail::getHtml));
            });

        if (attachmentNames != null && !attachmentNames.isEmpty()) {
            assertThat(mailsSentTo)
                .anySatisfy(mail -> assertThat(mail.getAttachments())
                    .extracting(Attachment::getName)
                    .containsExactlyInAnyOrderElementsOf(attachmentNames));
        }
    }
}
