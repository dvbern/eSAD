package ch.dvbern.oss.hemed.esc.api.shared.mail.service;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailHelperService {

    private final AppLanguageProvider appLanguageProvider;
    private final ConfigService configService;
    private final TLProducer tlProducer;

    private static final String HTML_BR = "<br>";
	private static final String LOG_PREFIX = "ESC-MAIL: Override E-Mail is enabled but no overriding recipients found, using original ";

    @NonNull
    public  List<String> prepareRecipients(@NonNull String to) {
        boolean overrideEmailsEnabled = configService.isMailOverridingRecipientsEnabled();
        if (overrideEmailsEnabled) {
            List<String> overridingRecipients = configService.getMailOverridingRecipientsAddress().stream()
                .filter(StringUtils::isNotBlank)
                .toList();
            if (overridingRecipients.isEmpty()) {
                LOG.error(LOG_PREFIX + "to recipients");
                return List.of(to);
            }
            LOG.info(
                "E-Mail wird nicht an Original-Empfänger verschickt, sondern an overriding recipients: {}",
                String.join(", ", overridingRecipients));
            return overridingRecipients;
        }
        return List.of(to);
    }

    @NonNull
    public String prepareSubject(@NonNull String subject) {
        if (!configService.isStufeProd()) {
            String prefix = String.format("eSAD%s: ", getStufePrefix());
            return prefix + subject;
        }
        return subject;
    }

    @NonNull
    public String prepareMessage(
        @NonNull String message,
        @NonNull String to
    ) {
        AppLanguages language = (AppLanguages) appLanguageProvider.current();
        TL tl = tlProducer.forAppLanguage(language);
        // Diese Nachricht wurde aus einem Testsystem versendet.
        String appendedTestInfo = appendInfoTestsystemToMessage(message, tl);
        // Sie erhalten diese Nachricht anstelle von ...
        return appendOverriddenRecipientInfo(appendedTestInfo, to, tl);
    }

    @NonNull
    private String appendInfoTestsystemToMessage(
        @NonNull String message,
        @NonNull TL tl
    ) {
        if (configService.isStufeProd()) {
            return message;
        }
        return message
            + HTML_BR
            + HTML_BR
            + tl.translate(
            I18nMessage.of("mail_info_testsystem_postfix"));
    }

    @NonNull
    private String appendOverriddenRecipientInfo(
        @NonNull String message,
        @NonNull String to,
        @NonNull TL tl
    ) {
        boolean overrideEmails = configService.isMailOverridingRecipientsEnabled();
        if (!overrideEmails) {
            return message;
        }
        return message
            + HTML_BR
            + HTML_BR
            + tl.translate(
            I18nMessage.of(
                "mail_info_overridden_recipient_info",
                "to", to
            ));
    }

    @NonNull
    private String getStufePrefix() {
        if (!configService.isStufeProd()) {
            return String.format(" (%s)", configService.getEnvironment());
        }
        return "";
    }
}
