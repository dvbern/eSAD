package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierMailService {
    private final TLProducer tlProducer;
    private final AppLanguageProvider appLanguageProvider;
    private final MailDAO mailDAO;
    private final TenantSettingsService tenantSettingsService;

    public void sendDossierQueueFinishedFailureMail(
        @NonNull DossierQueue dossierQueue
    ) {
        String base = "dossier-queue.mail.";
        String keyPrefix;
        String subject;
        String content;

        AppLanguages language = (AppLanguages) appLanguageProvider.current();
        TL tl = tlProducer.forAppLanguage(language);
        String email = tenantSettingsService.getTenantSetting().getSupportEmail();

        if (dossierQueue.getDossierQueueTyp() == DossierQueueTyp.ARCHIVIERUNG_DELETE) {
            keyPrefix = base + "archive.delete.failure";
        } else {
            keyPrefix = base + "failure";
        }
        content = tl.translate(
            keyPrefix + ".content",
            "uuid", dossierQueue.getDossierID(),
            "error", dossierQueue);
        subject = tl.translate(keyPrefix + ".subject");

        mailDAO.sendEmail(
                email,
                subject,
                content)
            .subscribeAsCompletionStage()
            .thenAccept(v -> LOG.debug("ESC-DOSSIERE-QUEUE: Success, E-mail sent to {}", email))
            .exceptionally(t -> {
                LOG.error("ESC-DOCUMENT-QUEUE: Error sending error mail to {}", email, t);
                return null;
            });
    }
}
