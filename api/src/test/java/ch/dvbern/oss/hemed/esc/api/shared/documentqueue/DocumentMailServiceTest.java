package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DocumentQueueBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class DocumentMailServiceTest extends AbstractITest {

    @Inject
    DocumentMailService documentMailService;

    private MailDAO mailDAOMock;
    private TL tlMock;

    private static final String PUBLIC_URL = "https://example.com";

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        ConfigService configServiceMock = mock(ConfigService.class);
        try {
            when(configServiceMock.getPublicUrl()).thenReturn(new URL(PUBLIC_URL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);

        mailDAOMock = mock(MailDAO.class);
        when(mailDAOMock.sendEmail(anyString(), anyString(), anyString()))
            .thenReturn(Uni.createFrom().voidItem());
        QuarkusMock.installMockForType(mailDAOMock, MailDAO.class);

        TLProducer tlProducerMock = mock(TLProducer.class);
        QuarkusMock.installMockForType(tlProducerMock, TLProducer.class);
        tlMock = mock(TL.class);
        when(tlProducerMock.forAppLanguage(any(AppLanguages.class))).thenReturn(tlMock);
    }

    @Test
    @TestAsAdminBS
    void sendDocumentQueueFinishedSuccessMail_should_use_success_key_for_non_massenbrief_type() {
        // Arrange
        DocumentQueue documentQueue = createDocumentQueue(FileTyp.PLAYGROUND);

        when(tlMock.translate(anyString(), anyString(), any())).thenReturn("translated content");
        when(tlMock.translate("document-queue.mail.success.subject")).thenReturn("translated subject");

        // Act
        documentMailService.sendDocumentQueueFinishedSuccessMail(documentQueue);

        // Assert
        String expectedEmail = documentQueue.getBenutzer().getEmail().getAddress();
        verify(tlMock).translate(
            "document-queue.mail.success.content",
            "link", PUBLIC_URL + "/report");
        verify(tlMock).translate("document-queue.mail.success.subject");
        verify(mailDAOMock).sendEmail(expectedEmail, "translated subject", "translated content");
    }

    @Test
    @TestAsAdminBS
    void sendDocumentQueueFinishedSuccessMail_should_use_massenbrief_success_key_for_massenbrief_zip_type() {
        // Arrange
        DocumentQueue documentQueue = createDocumentQueue(FileTyp.MASSENBRIEF_ZIP);

        when(tlMock.translate(anyString(), anyString(), any())).thenReturn("translated massenbrief content");
        when(tlMock.translate("document-queue.mail.massenbrief.success.subject"))
            .thenReturn("translated massenbrief subject");

        // Act
        documentMailService.sendDocumentQueueFinishedSuccessMail(documentQueue);

        // Assert
        String expectedEmail = documentQueue.getBenutzer().getEmail().getAddress();
        verify(tlMock).translate(
            "document-queue.mail.massenbrief.success.content",
            "link", PUBLIC_URL + "/report");
        verify(tlMock).translate("document-queue.mail.massenbrief.success.subject");
        verify(mailDAOMock).sendEmail(expectedEmail, "translated massenbrief subject", "translated massenbrief content");
    }

    @Test
    @TestAsAdminBS
    void sendDocumentQueueFinishedFailureMail_should_use_failure_key_for_non_massenbrief_type() {
        // Arrange
        DocumentQueue documentQueue = createDocumentQueue(FileTyp.PLAYGROUND);
        String errorMessage = "something went wrong";

        when(tlMock.translate(anyString(), anyString(), any())).thenReturn("translated error content");
        when(tlMock.translate("document-queue.mail.failure.subject")).thenReturn("translated error subject");

        // Act
        documentMailService.sendDocumentQueueFinishedFailureMail(documentQueue, errorMessage);

        // Assert
        String expectedEmail = documentQueue.getBenutzer().getEmail().getAddress();
        verify(tlMock).translate(
            "document-queue.mail.failure.content",
            "error", errorMessage);
        verify(tlMock).translate("document-queue.mail.failure.subject");
        verify(mailDAOMock).sendEmail(expectedEmail, "translated error subject", "translated error content");
    }

    @Test
    @TestAsAdminBS
    void sendDocumentQueueFinishedFailureMail_should_use_massenbrief_failure_key_for_massenbrief_zip_type() {
        // Arrange
        DocumentQueue documentQueue = createDocumentQueue(FileTyp.MASSENBRIEF_ZIP);
        String errorMessage = "massenbrief generation failed";

        when(tlMock.translate(anyString(), anyString(), any())).thenReturn("translated massenbrief error content");
        when(tlMock.translate("document-queue.mail.massenbrief.failure.subject"))
            .thenReturn("translated massenbrief error subject");

        // Act
        documentMailService.sendDocumentQueueFinishedFailureMail(documentQueue, errorMessage);

        // Assert
        String expectedEmail = documentQueue.getBenutzer().getEmail().getAddress();
        verify(tlMock).translate(
            "document-queue.mail.massenbrief.failure.content",
            "error", errorMessage);
        verify(tlMock).translate("document-queue.mail.massenbrief.failure.subject");
        verify(mailDAOMock).sendEmail(
            expectedEmail,
            "translated massenbrief error subject",
            "translated massenbrief error content");
    }

    @NonNull
    private DocumentQueue createDocumentQueue(@NonNull FileTyp fileTyp) {
        return DocumentQueueBuilder.create(builder -> builder.withFileTyp(fileTyp));
    }
}
