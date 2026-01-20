/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportReportDTO;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class MassenimportMailService {
    private final MailDAO mailDAO;
    private final ObjectMapper objectMapper;
    private final AppClock appClock;
    private final TL tl;

    private static final String SEND_SUCCESS_MAIL = "Massenimport success mail sent to {}";
    private static final String SEND_FAILURE_MAIL = "Massenimport failure mail sent to {}";
    private static final String SEND_ERROR = "Error while sending mail to {} for mass-import";

    public void sendSuccessMail(@NonNull MassenimportReportDTO resultDTO, @NonNull Benutzer benutzer) {
        String msg = tl.translate(
            "massenimport.mail.success.content",
            "name", benutzer.getNameVorname(),
            "totalRows", resultDTO.getTotalRows(),
            "skippedRows", resultDTO.getSkippedRows(),
            "importedKinder", resultDTO.getImportedKinder().size(),
            "importedKontaktpersonenKind", resultDTO.getImportedKontaktPersonenKind().size()
        );

        String emailAdress = benutzer.getEmail().getAddress();
        try {
            Path filePath = createReportFile(resultDTO);
            mailDAO.sendEmailWithAttachment(
                    emailAdress,
                    tl.translate("massenimport.mail.success.subject"),
                    msg,
                    List.of(filePath)
                ).subscribeAsCompletionStage()
                .thenAccept(v -> LOG.info(SEND_SUCCESS_MAIL, emailAdress))
                .exceptionally(t -> {
                    LOG.info(SEND_ERROR, emailAdress, t);
                    return null;
                });
        } catch (Exception e) {
            LOG.error(SEND_ERROR, emailAdress, e);
        }
    }

    public void sendFailureMail(@NonNull MassenimportReportDTO reportDTO, @NonNull Benutzer benutzer) {
        String emailAdress = benutzer.getEmail().getAddress();
        String technicalErrmsg = reportDTO.getExceptionMessage().isEmpty() ?
            "" :
            tl.translate("technical.error", "errorMessage", String.join(",", reportDTO.getExceptionMessage()));
        String msg = tl.translate(
            "massenimport.mail.failure.content",
            "name",
            benutzer.getNameVorname(),
            "errorMessage",
            technicalErrmsg);

        try {
            Path reportFile = createReportFile(reportDTO);

            mailDAO.sendEmailWithAttachment(
                    emailAdress,
                    tl.translate("massenimport.mail.failure.subject"),
                    msg,
                    List.of(reportFile)
                )
                .subscribeAsCompletionStage()
                .thenAccept(v -> LOG.info(SEND_FAILURE_MAIL, emailAdress))
                .exceptionally(t -> {
                    LOG.info(SEND_ERROR, emailAdress, t);
                    return null;
                });
        } catch (Exception e) {
            LOG.error(SEND_ERROR, emailAdress, e);
        }
    }

    @NonNull
    @SuppressWarnings("java:S5443")
    public Path createReportFile(@NonNull MassenimportReportDTO reportDTO) throws IOException {
        String fileName = "Massenimport_Report_" + appClock.localDateTime()
            .now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        Path tempFileLocation = Files.createTempFile(fileName, ".json");
        tempFileLocation.toFile().deleteOnExit();

        try {
            objectMapper.writeValue(tempFileLocation.toFile(), reportDTO);
            LOG.info("Created Massenimport report file at {}", tempFileLocation);
            return tempFileLocation;
        } catch (Exception e) {
            LOG.error("Error while creating Massenimport report file", e);
            throw e;
        }
    }

}
