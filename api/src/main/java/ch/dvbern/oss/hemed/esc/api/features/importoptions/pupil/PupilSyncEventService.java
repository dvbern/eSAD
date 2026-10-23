package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Stores an uploaded Pupil CSV: the file in S3 and the {@link PupilSyncEvent} pointing to it, in one transaction, so
 * the upload is deleted again if the event cannot be persisted.
 * <p>
 * A service of its own because {@link PupilService} runs without a transaction and must not call a transactional
 * method of its own (see TransactionsTest).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PupilSyncEventService {

    private final AppClock appClock;
    private final FileStoreService fileStoreService;
    private final PupilSyncEventRepo pupilSyncEventRepo;

    @NonNull
    public PupilSyncEvent savePupilSyncData(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(
            fileUpload,
            FileTyp.PUPIL_SYNC
        );
        // upload the file to S3; it is deleted again if the transaction does not commit
        PutObjectResponse putObjectResponse = fileStoreService.uploadToS3BoundToTransaction(
            fileUpload,
            fileInfo
        );
        LOG.info("Uploaded pupil csv file  to S3: {}", putObjectResponse);

        LocalDate today = appClock.getToday();
        final String originalFileName = fileInfo.getFileName().getFileName();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            List.of(),
            today,
            "Pupil-Sync",
            FileTyp.PUPIL_SYNC,
            originalFileName
        );

        FileData fileData = new FileData(
            FileTyp.PUPIL_SYNC,
            calculatedFileName,
            fileInfo
        );

        PupilSyncEvent event = new PupilSyncEvent(fileData);
        pupilSyncEventRepo.persist(event);
        return event;
    }
}
