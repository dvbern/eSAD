package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentMailService {
    private final TLProducer tlProducer;
    private final MailDAO mailDAO;
    private final ConfigService configService;

    public void sendDocumentQueueFinishedSuccessMail(@NonNull DocumentQueue documentQueue) {
        sendMailAsyncAndLog(documentQueue, true, null);
    }

    public void sendDocumentQueueFinishedFailureMail(
        @NonNull DocumentQueue documentQueue,
        @NonNull String errorMessage
    ) {
        sendMailAsyncAndLog(documentQueue, false, errorMessage);
    }

    private void sendMailAsyncAndLog(
        @NonNull DocumentQueue documentQueue,
        boolean success,
        @Nullable String errorMessage
    ) {
        String base = "document-queue.mail.";
        String keyPrefix;
        String subject;
        String content;
        TL tl = tlProducer.forAppLanguage(documentQueue.getLanguage());
        String email = documentQueue.getBenutzer().getEmail().getAddress();
        // This does not work locally for be because the publicUrl is missing the "-bs" suffix, only a local issue?
        String link = configService.getPublicUrl() + "/report";

        if (documentQueue.getType() == FileTyp.MASSENBRIEF_ZIP) {
            keyPrefix = base + "massenbrief." + (success ? "success" : "failure");
        } else {
            keyPrefix = base + (success ? "success" : "failure");
        }
        if (errorMessage == null) {
            content = tl.translate(keyPrefix + ".content", "link", link);
        } else {
            content = tl.translate(keyPrefix + ".content", "error", errorMessage);
        }
        subject = tl.translate(keyPrefix + ".subject");

        mailDAO.sendEmail(
                email,
                subject,
                content)
            .subscribeAsCompletionStage()
            .thenAccept(v -> LOG.debug("ESC-DOCUMENT-QUEUE: Success, mail sent to {}", email))
            .exceptionally(t -> {
                LOG.error("ESC-DOCUMENT-QUEUE: Error sending error mail to {}", email, t);
                return null;
            });

    }
}
