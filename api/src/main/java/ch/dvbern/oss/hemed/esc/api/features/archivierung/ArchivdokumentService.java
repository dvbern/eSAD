package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipOutputStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.S3ZipWriter;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.TempFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivdokumentService {

    private static final String BULK_DOWNLOAD_ZIP_FILENAME = "archivdokumente.zip";

    private final ArchivdokumentRepo archivdokumentRepo;
    private final ArchivdokumentMailService archivdokumentMailService;
    private final CurrentUserService currentUserService;
    private final FileStoreService fileStoreService;
    private final TempFileService tempFileService;
    private final ConfigService configService;
    private final AppClock appClock;

    @NonNull
    public Archivdokument persist(@NonNull Archivdokument dokument) {
        archivdokumentRepo.persist(dokument);
        return dokument;
    }

    @NonNull
    public List<Archivdokument> findAll() {
        return archivdokumentRepo.findAll();
    }


    @NonNull
    public List<Archivdokument> findAll(boolean onlyActive) {
        List<ArchivierungStatus> archivierungStatusList = ArchivierungStatus.offeneStatus();
        if (!onlyActive) {
            archivierungStatusList = ArchivierungStatus.abgeschlosseneStatus();
        }

        Benutzer currentUser = currentUserService.currentUser();
        if (currentUser.hasFachRolleSupport()) {
            return archivdokumentRepo.findAll(archivierungStatusList);
        }

        return archivdokumentRepo.findAllForBenutzer(currentUser.getId(), archivierungStatusList);
    }

    /**
     * Finds the open archive documents that should be bundled into a single zip for a bulk download.
     * <p>
     * A soft cap (see {@code esc.archive.bulk-download.max-files}) is applied: to avoid loading and zipping an
     * unbounded amount of files into memory, at most {@code maxFiles} documents are returned. When the user has a
     * larger backlog, the list is silently capped; the frontend informs the user upfront (in the download confirmation
     * dialog) that only the first {@code maxFiles} documents will be included so they can download the remaining
     * documents in a subsequent batch.
     */
    @NonNull
    public List<ID<Archivdokument>> findAllForBulkDownload() {
        int maxFiles = configService.getArchiveBulkDownloadMaxFiles();
        return archivdokumentRepo.findAllArchivdokIDsForBenutzer(currentUserService.currentUser().getId(), ArchivierungStatus.offeneStatus(), maxFiles);
    }

    @NonNull
    public Archivdokument getById(@NonNull ID<Archivdokument> archivdokumentID) {
        return archivdokumentRepo.getById(archivdokumentID);
    }

    public void sendArchiveDocumentDownloadReminder() {
        List<Benutzer> benutzerWithOffeneDokumente =
            archivdokumentRepo.findArchivOwnersHavingDocumentsInStatus(ArchivierungStatus.OFFEN);
        archivdokumentMailService.sendArchiveDocumentDownloadReminder(benutzerWithOffeneDokumente);
    }

    @NonNull
    public List<ID<Dossier>> findKinderAelterAlsUndNichtArchiviert(int minAge, int batchsize) {
        return archivdokumentRepo.findKinderAelterAlsUndNichtArchiviert(minAge, batchsize);
    }

    public void setArchivierungStatusAndTimestamp(@NonNull Archivdokument archivdokument) {
        if (archivdokument.getArchivierungStatus() == ArchivierungStatus.OFFEN) {
            archivdokument.setTimestampDownloaded(appClock.offsetDateTime().now());
            if (archivdokument.getArchivierungTyp() == ArchivierungTyp.KIND_DEAKTIVIERT) {
                archivdokument.setArchivierungStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
            } else {
                archivdokument.setArchivierungStatus(ArchivierungStatus.DOWNLOADED);
            }
            persist(archivdokument);
        }
    }

    public List<ID<Dossier>> findKinderReadyToDelete(int monthsBeforeDeletion, int batchsize) {
        return archivdokumentRepo.findKinderThatAreReadyToDelete(monthsBeforeDeletion, batchsize);
    }

    public void removeForDossier(@NonNull ID<Dossier> dossierID) {
        List<Archivdokument> archivdokumentList = archivdokumentRepo.findAllForDossier(dossierID);
        for (Archivdokument archivdokument : archivdokumentList) {
            archivdokumentRepo.remove(archivdokument.getId());
        }
    }

    @NonNull
    public FileInfo downloadArchivFile(@NonNull ID<Archivdokument> archivdokumentId) {
        Archivdokument archivdokument = getById(archivdokumentId);
        if (archivdokument.getFiledata() == null) {
            throw new AppFailureException(AppFailureMessage.entityNotFound(archivdokumentId, "File not found"));
        }
        setArchivierungStatusAndTimestamp(archivdokument);
        return archivdokument.getFiledata().getFileInfoBucket();
    }

    /**
     * Downloads all given archive documents, marks them as downloaded and packs their files into a single zip archive.
     * <p>
     * The archive is written to a temp file rather than to the heap: with a cap of several hundred largely
     * incompressible PDFs, keeping the zip in memory would peak at several times the payload size and take the whole
     * instance down with it. The caller owns the returned file and must delete it once it has been streamed out.
     */
    @NonNull
    public Path downloadArchivFilesAsZip(@NonNull List<ID<Archivdokument>> archivdokumentIds) {
        Path zipFile = tempFileService.createTempFile(new CleanFileName(BULK_DOWNLOAD_ZIP_FILENAME));

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipFile)))) {
            S3ZipWriter zipWriter = new S3ZipWriter(zos, fileStoreService);
            for (ID<Archivdokument> archivdokumentId : archivdokumentIds) {
                addArchivdokumentToZip(zipWriter, archivdokumentId);
            }
        } catch (IOException e) {
            tempFileService.deleteQuietly(zipFile);
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not create zip archive for bulk Archivdokument download"),
                e
            );
        } catch (RuntimeException e) {
            tempFileService.deleteQuietly(zipFile);
            throw e;
        }

        return zipFile;
    }

    private void addArchivdokumentToZip(
        @NonNull S3ZipWriter zipWriter,
        @NonNull ID<Archivdokument> archivdokumentId
    ) throws IOException {
        Archivdokument archivdokument = getById(archivdokumentId);
        FileData fileData = archivdokument.getFiledata();
        if (fileData == null) {
            LOG.error("Missing FileData for Archivdokument [{}]", archivdokumentId);
            return;
        }

        FileInfo fileInfo = fileData.getFileInfoBucket();
        if (!zipWriter.addFromS3IfAvailable(fileInfo.getFileName().getFileName(), fileInfo)) {
            LOG.error("Skipping Archivdokument [{}], its file could not be read from S3", archivdokumentId);
            return;
        }

        setArchivierungStatusAndTimestamp(archivdokument);
    }
}
