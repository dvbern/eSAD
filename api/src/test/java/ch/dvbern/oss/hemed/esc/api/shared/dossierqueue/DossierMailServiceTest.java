package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.util.UUID;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class DossierMailServiceTest extends AbstractITest {

    @Inject
    DossierMailService dossierMailService;

    private MailDAO mailDAOMock;
    private TL tlMock;

    private static final String SUPPORT_EMAIL = "support@example.com";

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        TenantSettingsService tenantSettingsServiceMock = mock(TenantSettingsService.class);
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);
        TenantSetting tenantSettingMock = mock(TenantSetting.class);
        when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSettingMock);
        when(tenantSettingMock.getSupportEmail()).thenReturn(SUPPORT_EMAIL);

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
    void sendDossierQueueFinishedFailureMail_should_use_failure_key_for_non_archive_delete_type() {
        // Arrange
        DossierQueue dossierQueue = createDossierQueue(DossierQueueTyp.ARCHIVIERUNG);

        when(tlMock.translate(
            anyString(), anyString(), any(), anyString(), any()
        )).thenReturn("translated content");
        when(tlMock.translate("dossier-queue.mail.failure.subject")).thenReturn("translated subject");

        // Act
        dossierMailService.sendDossierQueueFinishedFailureMail(dossierQueue);

        // Assert
        verify(tlMock).translate(
            "dossier-queue.mail.failure.content",
            "uuid", dossierQueue.getDossierID(),
            "error", dossierQueue);
        verify(tlMock).translate("dossier-queue.mail.failure.subject");
        verify(mailDAOMock).sendEmail(SUPPORT_EMAIL, "translated subject", "translated content");
    }

    @Test
    @TestAsAdminBS
    void sendDossierQueueFinishedFailureMail_should_use_archive_delete_failure_key_for_archive_delete_type() {
        // Arrange
        DossierQueue dossierQueue = createDossierQueue(DossierQueueTyp.ARCHIVIERUNG_DELETE);

        when(tlMock.translate(
            anyString(), anyString(), any(), anyString(), any()
        )).thenReturn("translated archive delete content");
        when(tlMock.translate("dossier-queue.mail.archive.delete.failure.subject"))
            .thenReturn("translated archive delete subject");

        // Act
        dossierMailService.sendDossierQueueFinishedFailureMail(dossierQueue);

        // Assert
        verify(tlMock).translate(
            "dossier-queue.mail.archive.delete.failure.content",
            "uuid", dossierQueue.getDossierID(),
            "error", dossierQueue);
        verify(tlMock).translate("dossier-queue.mail.archive.delete.failure.subject");
        verify(mailDAOMock).sendEmail(
            SUPPORT_EMAIL,
            "translated archive delete subject",
            "translated archive delete content");
    }

    @Test
    @TestAsAdminBS
    void sendDossierQueueFinishedFailureMail_should_use_failure_key_for_verantw_changed_type() {
        // Arrange
        DossierQueue dossierQueue = createDossierQueue(DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE);

        when(tlMock.translate(
            anyString(), anyString(), any(), anyString(), any()
        )).thenReturn("translated content");
        when(tlMock.translate("dossier-queue.mail.failure.subject")).thenReturn("translated subject");

        // Act
        dossierMailService.sendDossierQueueFinishedFailureMail(dossierQueue);

        // Assert
        verify(tlMock).translate(
            "dossier-queue.mail.failure.content",
            "uuid", dossierQueue.getDossierID(),
            "error", dossierQueue);
        verify(tlMock).translate("dossier-queue.mail.failure.subject");
        verify(mailDAOMock).sendEmail(SUPPORT_EMAIL, "translated subject", "translated content");
    }

    @NonNull
    private DossierQueue createDossierQueue(@NonNull DossierQueueTyp typ) {
        DossierQueue dossierQueue = DossierQueue.of(UUID.randomUUID(), typ, null);
        dossierQueue.setId(ID.of(UUID.randomUUID(), DossierQueue.class));
        return dossierQueue;
    }
}
