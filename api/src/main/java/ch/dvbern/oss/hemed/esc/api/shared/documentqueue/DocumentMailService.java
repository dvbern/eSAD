package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentMailService {
    private final TLProducer tlProducer;
    private final MailDAO mailDAO;
    private final ConfigService configService;

    public void sendDocumentQueueFinishedSuccessMail(@NonNull DocumentQueue documentQueue) {
        sendMailAsyncAndLog(documentQueue, "document-queue.mail.success");
    }

    public void sendDocumentQueueFinishedFailureMail(@NonNull DocumentQueue documentQueue) {
        sendMailAsyncAndLog(documentQueue, "document-queue.mail.failure");
    }

    private void sendMailAsyncAndLog(@NonNull DocumentQueue documentQueue, @NonNull String keyPrefix) {
        TL tl = tlProducer.forAppLanguage(documentQueue.getLanguage());
        String email = documentQueue.getBenutzer().getEmail().getAddress();
        // This does not work locally for be because the publicUrl is missing the "-bs" suffix, only a local issue?
        String link = configService.getPublicUrl() + "/report";
        mailDAO.sendEmail(
                email,
                tl.translate(keyPrefix + ".subject"),
                tl.translate(keyPrefix + ".content", "link", link))
            .subscribeAsCompletionStage()
            .thenAccept(v -> LOG.debug("ESC-DOCUMENT-QUEUE: Success mail sent to {}", email))
            .exceptionally(t -> {
                LOG.error("ESC-DOCUMENT-QUEUE: Error sending error mail to {}", email, t);
                return null;
            });
    }
}
