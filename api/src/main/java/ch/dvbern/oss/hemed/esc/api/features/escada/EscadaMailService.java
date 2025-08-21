package ch.dvbern.oss.hemed.esc.api.features.escada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.escada.json.EscadaImportResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
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
class EscadaMailService {

    public static final String ERROR_SENDING_ERROR_MAIL_TO = "Error sending error mail to {}";

    private final MailDAO mailDAO;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;
    private final AppClock appClock;

    public void sendWarningMail(@NonNull EscadaImportResultJson resultReport) {
        String msg = "Dear Admin\nThe following errors occurred during the import of the Escada data:\n\n"
            + resultReport.getImportSummary();

        configService.getCustomerAdminEmails().forEach(email -> {
            try {
                Path jsonFilePath =
                    createJsonFile(resultReport.toErrorFilteredEscadaImportResultJson(), "escada_import_error_report");
                mailDAO.sendEmailWithAttachment(
                        email, "Escada Import Error Report", msg,
                        List.of(jsonFilePath))
                    .subscribeAsCompletionStage()
                    .thenAccept(v -> LOG.info("Escada Import-Error-Report mail sent to {}", email))
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
            "<pre>Dear Admin\nThe following error occurred during the import of the Escada data:\n\n %s \n</pre>";
        String msg = msgTmpl.formatted(errormsg);
        configService.getCustomerAdminEmails().forEach(email -> {
            try {
                mailDAO.sendEmail(email, "Escada Import Fatal Error", msg)
                    .subscribeAsCompletionStage()
                    .thenAccept(v -> LOG.info("Escada Import-Error-Report mail sent to {}", email))
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
    private Path createJsonFile(@NonNull EscadaImportResultJson dto, @NonNull String fileName) throws IOException {
        fileName += '_' + appClock.localDateTime().now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
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
