package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class FachrollenMailService {

    private final TL tl;
    private final ConfigService configService;
    private final MailDAO mailDAO;
    private final TenantSettingsService tenantSettingsService;

    public void sendEinladungOrganisationMail(@NonNull Benutzer benutzer, @NonNull Organisation organisation) {
        // Mail nur senden wenn der Benutzer nicht der Verantwortliche Arzt ist -> Also in eine bestehende
        // Organisation hinzugefügt worden ist.
        Benutzer verantwortlicherArzt = organisation.getVerantwortlicherArzt();
        if (benutzer.equals(verantwortlicherArzt)) {
            return;
        }

        String base = "benutzer-organisation-delegieren.mail.";

        String link = configService.getPublicUrl().toString();
        String eMailBenutzer = benutzer.getEmail().getAddress();
        String eMailSupport = tenantSettingsService.getTenantSetting().getSupportEmail();
        String nameArzt = verantwortlicherArzt.getVorname() + ' ' + verantwortlicherArzt.getNachname();

        String subject = tl.translate(base + "subject");
        String content = tl.translate(
            base + "content",
            "nameArzt", nameArzt,
            "eMailBenutzer", eMailBenutzer,
            "linkeSAD", link,
            "eMailsupport", eMailSupport
        );

        mailDAO.sendEmail(
                eMailBenutzer,
                subject,
                content
            ).subscribeAsCompletionStage()
            .thenAccept(v -> LOG.debug(
                "BENUTZER-ORGANISATION-DELEGIEREN: Success, E-mail sent to {}",
                eMailBenutzer))
            .exceptionally(t -> {
                LOG.error("BENUTZER-ORGANISATION-DELEGIEREN: Error sending error mail to {}", eMailBenutzer, t);
                return null;
            });
    }
}
