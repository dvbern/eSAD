package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.json.PupilImportResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
public class PupilMailService {
    public static final String ERROR_SENDING_ERROR_MAIL_TO = "Error sending error mail to {}";

    private final MailDAO mailDAO;
    private final ObjectMapper objectMapper;
    private final AppClock appClock;
    private final TenantSettingsService tenantSettingsService;

    public void sendWarningMail(@NonNull PupilImportResultJson resultReport) {
        String msg = "Dear Admin\t\nThe following errors occurred during the import of the Pupil data:\t\n\n"
            + resultReport.getImportSummary();

        tenantSettingsService.getTenantSetting().getCustomerAdminEmails().forEach(email -> {
            try {
                Path jsonFilePath =
                    createJsonFile(resultReport.toErrorFilteredPupilImportResultJson(), "pupil_import_error_report");
                mailDAO.sendEmailWithAttachment(
                        email, "Pupil Import Error Report", msg,
                        List.of(jsonFilePath))
                    .subscribeAsCompletionStage()
                    .thenAccept(v -> LOG.info("Pupil Import-Waring-Report mail sent to {}", email))
                    .exceptionally(t -> {
                        LOG.info(ERROR_SENDING_ERROR_MAIL_TO, email, t);
                        return null;
                    });

            } catch (Exception e) {
                LOG.error(ERROR_SENDING_ERROR_MAIL_TO, email, e);
            }
        });
    }

    public void sendErrorMail(@NonNull String errormsg) {
        String msgTmpl =
            "<pre>Dear Admin\t\nThe following error occurred during the import of the Pupil data:\t\n\n %s \t\n</pre>";
        String msg = msgTmpl.formatted(errormsg);
        tenantSettingsService.getTenantSetting().getCustomerAdminEmails().forEach(email -> {
            try {
                mailDAO.sendEmail(email, "Pupil Import Fatal Error", msg)
                    .subscribeAsCompletionStage()
                    .thenAccept(v -> LOG.info("Pupil Import-Error-Report mail sent to {}", email))
                    .exceptionally(t -> {
                        LOG.info(ERROR_SENDING_ERROR_MAIL_TO, email, t);
                        return null;
                    });

            } catch (Exception e) {
                LOG.error(ERROR_SENDING_ERROR_MAIL_TO, email, e);
            }
        });
    }

    @NonNull
    private Path createJsonFile(@NonNull PupilImportResultJson dto, @NonNull String fileName) throws IOException {
        fileName += '_' + appClock.localDateTime().now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm", Locale.GERMAN));
        Path tempFileLoc = Files.createTempFile(fileName, ".json");
        tempFileLoc.toFile().deleteOnExit();

        try {
            objectMapper.writeValue(tempFileLoc.toFile(), dto);
            LOG.debug("Created JSON file at: {}", tempFileLoc);
            return tempFileLoc;
        } catch (IOException e) {
            LOG.error("Error creating JSON file", e);
            throw e;
        }
    }
}
