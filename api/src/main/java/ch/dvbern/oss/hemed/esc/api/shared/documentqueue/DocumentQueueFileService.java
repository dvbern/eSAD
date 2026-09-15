/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.TempFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService.unmarshalDocumentQueueCsvParameter;
import static ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService.unmarshalDocumentQueueParameter;
import static ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService.unmarshallDocumentQueueMassenbriefParameter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentQueueFileService {

    private final BriefvorlageService briefvorlageService;
    private final AppClock appClock;
    private final TL tl;
    private final TempFileService tempFileService;

    @NonNull
    public Path createTempFile(@NonNull CleanFileName fileName, byte[] content) {
        return tempFileService.createTempFile(fileName, content);
    }

    @NonNull
    public Path createTempFile(@NonNull CleanFileName fileName) {
        return tempFileService.createTempFile(fileName);
    }

    public CleanFileName getCleanFileNameFromDocumentQueue(
        @NonNull DocumentQueue documentQueue
    ) {
        String baseFileName = switch (documentQueue.getType()) {
            case PLAYGROUND -> "Playground";
            case PLAYGROUND_ERROR -> "Playground Fehler";
            case SPSS_REPORT -> {
                DocumentQueueParams params = unmarshalDocumentQueueParameter(documentQueue);
                Objects.requireNonNull(params.getSpssReportType());
                String suffix = tl.translateValue(params.getSpssReportType());

                yield "Spss-Report-" + suffix;
            }
            case CSV_REPORT -> {
                DocumentQueueCsvParams params = unmarshalDocumentQueueCsvParameter(documentQueue);
                Objects.requireNonNull(params.getCsvReportType());
                String suffix = tl.translateValue(params.getCsvReportType());

                yield "CSV-Report-" + suffix;
            }
            case MASSENBRIEF_ZIP -> {
                DocumentQueueMassenbriefSaveJson params = unmarshallDocumentQueueMassenbriefParameter(documentQueue);
                Briefvorlage briefvorlage = briefvorlageService.getById(params.getBriefvorlageID());

                yield "Massenbrief-" + briefvorlage.getName();
            }
            default -> throw new AppFailureException(AppFailureMessage.internalError(
                "Unhandled FileType in DocumentQueue getNameVorname switch: " + documentQueue.getType()));
        };
        String fileExtension = switch (documentQueue.getType()) {
            case PLAYGROUND, PLAYGROUND_ERROR -> ".pdf";
            case SPSS_REPORT, CSV_REPORT -> ".csv";
            case MASSENBRIEF_ZIP -> ".zip";
            default -> throw new AppFailureException(AppFailureMessage.internalError(
                "Unhandled FileType in DocumentQueue getFileExtension switch: " + documentQueue.getType()));
        };

        return FileNameUtil.calculateAutomaticFilename(
            List.of(),
            appClock.getToday(),
            baseFileName,
            documentQueue.getType(),
            fileExtension);
    }
}
