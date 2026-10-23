package ch.dvbern.oss.hemed.esc.api.features.notizen;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizListItemJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotizService {

    private final NotizRepo notizRepo;

    private final FileStoreService fileStoreService;
    private final FileValidationService fileValidationService;
    private final AppClock appClock;
    private final FileDataService fileDataService;

    @NonNull
    public Notiz persist(@NonNull Notiz notiz) {
        notizRepo.persist(notiz);
        return notiz;
    }

    @NonNull
    public Notiz persistWithOptimisticLocking(@NonNull Notiz notiz, @NonNull Long versionFromFrontend) {
        return notizRepo.persistWithOptimisticLocking(notiz, versionFromFrontend);
    }

    @NonNull
    public Notiz updateWithFiles(
        @NonNull Notiz notiz,
        @NonNull List<FileUpload> fileUploads,
        @NonNull Long versionFromFrontend
    ) {
        Notiz savedNotiz = notizRepo.persistWithOptimisticLocking(notiz, versionFromFrontend);
        for (FileUpload fileUpload : fileUploads) {
            savedNotiz = this.addUploadedFileAndFlush(savedNotiz.getId(), fileUpload);
        }
        notizRepo.flush();
        return savedNotiz;
    }

    public void remove(@NonNull ID<Notiz> notizId, boolean deleteFilesInS3) {
        Notiz notizToDelete = notizRepo.getById(notizId);
        // Die Notiz entfernen
        notizRepo.remove(notizId);
        // Die Files in S3 entfernen
        fileDataService.deleteFileData(notizToDelete.getFiles(), deleteFilesInS3);
        LOG.info("Removed notiz {} and deleted files in S3", notizId);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<Notiz> notizen = findAllForKind(kindID);
        for (Notiz notiz : notizen) {
            remove(notiz.getId(), true);
        }
    }

    @NonNull
    public Optional<Notiz> findById(@NonNull ID<Notiz> notizId) {
        return notizRepo.findById(notizId);
    }

    @NonNull
    public Notiz getById(@NonNull ID<Notiz> id) {
        return notizRepo.getById(id);
    }

    @NonNull
    public List<NotizListItemJson> findAllListItems() {
        return notizRepo.findAllListItems();
    }

    @NonNull
    public List<Notiz> findAllForKind(@NonNull ID<Kind> kindID) {
        return notizRepo.findAllForKind(kindID);
    }


    @NonNull
    public Notiz addUploadedFileAndFlush(@NonNull ID<Notiz> notizID, @NonNull FileUpload file) {
        Notiz updatedNotiz = addUploadedFile(notizID, file);
        notizRepo.flush();
        return updatedNotiz;
    }

    @NonNull
    public Notiz addUploadedFile(@NonNull ID<Notiz> notizID, @NonNull FileUpload file) {
        fileValidationService.requireFiletypeIsAllowed(file, FileTyp.NOTIZUPLOAD);
        FileInfo fileInfo = saveNotizAttachmentInFilestorage(file);
        Notiz notiz = notizRepo.getById(notizID);
        notiz.disableOptimisticLockingCheck();
        FileTyp fileTyp = FileNameUtil.getTypFromLabel(notiz.getLabels(), FileTyp.NOTIZUPLOAD);
        String nameVorname = notiz.getDossier().getKind().getNameVorname();
        String originalFileName = fileInfo.getFileName().getFileName();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            notiz.getFiles(),
            appClock.getToday(),
            nameVorname,
            fileTyp,
            originalFileName);

        FileData filedata = new FileData(fileTyp, calculatedFileName, fileInfo);

        return notizRepo.addUploadedFile(notizID, filedata);
    }

    public void removeUploadedFile(@NonNull Notiz loadedNotiz, @NonNull ID<FileData> fileId) {
        FileInfo fileToRemove = findUploadedFileInfo(loadedNotiz, fileId);
        loadedNotiz.disableOptimisticLockingCheck();
        boolean removedFromList = loadedNotiz.getFiles().removeIf(fileData -> fileId.equals(fileData.getId()));
        if (!removedFromList) {
            throw new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found"));
        }

        notizRepo.flush();

        // only once the removal is committed: deleting now would lose the file if the transaction still rolls back
        fileStoreService.deleteInS3AfterCommit(fileToRemove.getFileIdInBucket());
    }

    @NonNull
    private FileInfo saveNotizAttachmentInFilestorage(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.NOTIZUPLOAD);
        // upload the file to S3; it is deleted again if the transaction does not commit
        PutObjectResponse putObjectResponse = fileStoreService.uploadToS3BoundToTransaction(
            fileUpload,
            fileInfo
        );
        LOG.info("Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}", fileInfo.getFileIdInBucket(), putObjectResponse);
        return fileInfo;
    }

    @NonNull
    public FileInfo findUploadedFileInfo(
        @NonNull Notiz notiz,
        @NonNull ID<FileData> fileId
    ) {
        return this.findUploadedFileData(notiz, fileId)
            .getFileInfoBucket();
    }

    @NonNull
    public FileData findUploadedFileData(
        @NonNull Notiz notiz,
        @NonNull ID<FileData> fileId
    ) {
        return notiz.getFiles().stream()
            .filter(fileData -> fileData.getId().equals(fileId))
            .findFirst()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found")));

    }
}
