package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ArchivdokumentMailService {

    public static final String ERROR_SENDING_ERROR_MAIL_TO = "Error sending archiv document download reminder  mail to {}";
    public static final String SUCCESS_SENDING_ERROR_MAIL_TO = "Error sending archiv document download reminder  mail to {}";

    private final MailDAO mailDAO;
    private final ConfigService configService;
    private final TenantSettingsService tenantSettingsService;
    private final TLProducer tlProducer;

    public void sendArchiveDocumentDownloadReminder(List<Benutzer> benutzerWithOffeneDokumente) {
        List<String> emailSubject = new ArrayList<>();
        List<String> emailContent = new ArrayList<>();

        tenantSettingsService.getTenantSetting().getSupportedLanguages().forEach(
            appLanguage -> {
                TL tl = tlProducer.forAppLanguage(appLanguage);
                emailSubject.add(tl.translate("archivdokument.reminder.email.subject"));
                emailContent.add(tl.translate(
                    "archivdokument.reminder.email.content",
                    "link", configService.getPublicUrl() + "/admin/archivierung/offen",
                    "mailsupport", tenantSettingsService.getTenantSetting().getSupportEmail()
                ));
            }
        );

        benutzerWithOffeneDokumente.forEach( benutzer -> {
            String email = benutzer.getEmail().getAddress();

            try {
                mailDAO.sendEmail(
                    email,
                        String.join(" / ", emailSubject),
                        String.join("<hr>", emailContent)
                )
                .subscribeAsCompletionStage()
                .thenAccept(v -> LOG.info(SUCCESS_SENDING_ERROR_MAIL_TO, email))
                .exceptionally(t -> {
                    LOG.error( t.getMessage());
                    LOG.info(ERROR_SENDING_ERROR_MAIL_TO, email, t);
                    return null;
                });

            } catch (Exception e) {
                LOG.error(ERROR_SENDING_ERROR_MAIL_TO, email, e);
            }
        });
    }
}
