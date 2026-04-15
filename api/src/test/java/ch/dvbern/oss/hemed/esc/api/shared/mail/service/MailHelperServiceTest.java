package ch.dvbern.oss.hemed.esc.api.shared.mail.service;

import java.util.Collections;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailHelperServiceTest {

    @Mock
    private AppLanguageProvider appLanguageProvider;

    @Mock
    private ConfigService configService;

    @Mock
    private TLProducer tlProducer;

    @Mock
    private TL tl;

    @Mock
    private TenantProducer tenantProducer;

    @InjectMocks
    private MailHelperService mailHelperService;


    @Test
    void prepareRecipients_overridingDisabled_returnsOriginalTo() {
        // Arrange
        String originalTo = "original@example.com";
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(false);

        // Act
        List<String> result = mailHelperService.prepareRecipients(originalTo);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst()).isEqualTo(originalTo);
    }

    @Test
    void prepareRecipients_overridingEnabledWithRecipients_returnsOverridingTo() {
        // Arrange
        String originalTo = "original@example.com";
        String overridingTo = "override@example.com";
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(true);
        when(configService.getMailOverridingRecipientsAddress()).thenReturn(List.of(overridingTo));

        // Act
        List<String> result = mailHelperService.prepareRecipients(originalTo);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst()).isEqualTo(overridingTo);

    }

    @Test
    void prepareRecipients_overridingEnabledWithEmptyRecipients_returnsOriginalTo() {
        // Arrange
        String originalTo = "original@example.com";
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(true);
        when(configService.getMailOverridingRecipientsAddress()).thenReturn(Collections.emptyList());

        // Act
        var result = mailHelperService.prepareRecipients(originalTo);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst()).isEqualTo(originalTo);
    }

    @Test
    void prepareRecipients_overridingEnabledWithEmptyStringRecipients_returnsOriginalTo() {
        // Arrange
        String originalTo = "original@example.com";
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(true);
        when(configService.getMailOverridingRecipientsAddress()).thenReturn(List.of(""));

        // Act
        List<String> result = mailHelperService.prepareRecipients(originalTo);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst()).isEqualTo(originalTo);
    }

    @Test
    void prepareSubject_productionEnvironment_returnsOriginalSubject() {
        // Arrange
        String originalSubject = "Test Subject";
        when(configService.isStufeProd()).thenReturn(true);

        // Act
        String result = mailHelperService.prepareSubject(originalSubject);

        // Assert
        assertEquals(originalSubject, result);
    }

    @Test
    void prepareSubject_nonProductionEnvironment_returnsSubjectWithPrefix() {
        // Arrange
        String originalSubject = "Test Subject";
        when(configService.isStufeProd()).thenReturn(false);
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.UNIT_TEST);
        when(tenantProducer.produceTenant()).thenReturn(TenantEnum.BS);

        // Act
        String result = mailHelperService.prepareSubject(originalSubject);

        // Assert
        assertEquals("eSAD (UNIT_TEST, BS): Test Subject", result);
    }

    @Test
    void prepareMessage_productionWithoutOverride_returnsOriginalMessage() {
        // Arrange
        String originalMessage = "Test Message";
        String to = "test@example.com";
        when(configService.isStufeProd()).thenReturn(true);
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(false);

        // Act
        String result = mailHelperService.prepareMessage(originalMessage, to);

        // Assert
        assertEquals(originalMessage, result);
    }

    @Test
    void prepareMessage_nonProductionWithoutOverride_appendsTestInfo() {
        // Arrange
        String originalMessage = "Test Message";
        String to = "test@example.com";
        String testInfo = "This message was sent from a test system.";
        when(configService.isStufeProd()).thenReturn(false);
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(false);
        when(appLanguageProvider.current()).thenReturn(AppLanguages.DE);
        when(tlProducer.forAppLanguage(any(AppLanguages.class))).thenReturn(tl);
        when(tl.translate(any(I18nMessage.class))).thenReturn(testInfo);

        // Act
        String result = mailHelperService.prepareMessage(originalMessage, to);

        // Assert
        assertEquals("Test Message<br><br>This message was sent from a test system.", result);
    }

    @Test
    void prepareMessage_productionWithOverride_appendsOverrideInfo() {
        // Arrange
        String originalMessage = "Test Message";
        String to = "test@example.com";
        String overrideInfo = "You are receiving this message instead of test@example.com";
        when(configService.isStufeProd()).thenReturn(true);
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(true);
        when(appLanguageProvider.current()).thenReturn(AppLanguages.DE);
        when(tlProducer.forAppLanguage(any(AppLanguages.class))).thenReturn(tl);
        when(tl.translate(any(I18nMessage.class))).thenReturn(overrideInfo);

        // Act
        String result = mailHelperService.prepareMessage(originalMessage, to);

        // Assert
        assertEquals("Test Message<br><br>You are receiving this message instead of test@example.com", result);
    }

    @Test
    void prepareMessage_nonProductionWithOverride_appendsBothInfos() {
        // Arrange
        String originalMessage = "Test Message";
        String to = "test@example.com";
        String testInfo = "This message was sent from a test system.";
        String overrideInfo = "You are receiving this message instead of test@example.com";
        when(configService.isStufeProd()).thenReturn(false);
        when(configService.isMailOverridingRecipientsEnabled()).thenReturn(true);
        when(appLanguageProvider.current()).thenReturn(AppLanguages.DE);
        when(tlProducer.forAppLanguage(any(AppLanguages.class))).thenReturn(tl);
        when(tl.translate(any(I18nMessage.class)))
            .thenReturn(testInfo)
            .thenReturn(overrideInfo);

        // Act
        String result = mailHelperService.prepareMessage(originalMessage, to);

        // Assert
        assertEquals("Test Message<br><br>This message was sent from a test system.<br><br>You are receiving this message instead of test@example.com", result);
    }
}

