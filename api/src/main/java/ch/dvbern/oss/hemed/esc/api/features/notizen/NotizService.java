package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotizService {

    private final NotizRepo notizRepo;

    private final FileStoreService fileStoreService;
    private final FileValidationService fileValidationService;
    private final ConfigService configService;
    private final AppClock appClock;

    @NonNull
    public Notiz persist(@NonNull Notiz notiz) {
        notizRepo.persist(notiz);
        return notiz;
    }

    @NonNull
    public Notiz updateWithFiles(
        @NonNull Notiz notiz,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz savedNotiz = this.persist(notiz);
        for (FileUpload fileUpload : fileUploads) {
            savedNotiz = this.addUploadedFile(savedNotiz.getId(), fileUpload);
        }
        return savedNotiz;
    }

    public void remove(@NonNull ID<Notiz> notizId) {
        notizRepo.remove(notizId);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<Notiz> notizen = findAllForKind(kindID);
        for (Notiz notiz : notizen) {
            remove(notiz.getId());
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
    public List<Notiz> findAllForKind(@NonNull ID<Kind> kindID) {
        return notizRepo.findAllForKind(kindID);
    }

    @NonNull
    public Notiz addUploadedFile(@NonNull ID<Notiz> notizID, @NonNull FileUpload file) {
        fileValidationService.requireFiletypeIsAllowed(file, FileTyp.NOTIZUPLOAD);
        FileInfoS3 fileInfoS3 = saveNotizAttachmentInFilestorage(file);
        Notiz notiz = notizRepo.getById(notizID);
        FileTyp fileTyp = FileNameUtil.getTypFromLabel(notiz.getLabels(), FileTyp.NOTIZUPLOAD);
        String nameVorname = notiz.getDossier().getKind().getNameVorname();
        String originalFileName = fileInfoS3.getFileName().getFileName();
        String calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            notiz.getFiles(),
            appClock.localDateTime().now().toLocalDate(),
            nameVorname,
            fileTyp,
            originalFileName);

        FileData filedata = new FileData(fileTyp, calculatedFileName, fileInfoS3);

        return notizRepo.addUploadedFile(notizID, filedata);
    }

    public void removeUploadedFile(@NonNull Notiz loadedNotiz, @NonNull ID<FileData> fileId) {
        FileInfoS3 fileToRemove = findUploadedFileInfoS3(loadedNotiz.getId(), fileId);
        boolean removedFromList = loadedNotiz.getFiles().removeIf(fileData -> fileId.equals(fileData.getId()));
        if (!removedFromList) {
            throw new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found"));
        }

        Uni<DeleteObjectResponse> deleteObjectResponseUni =
            fileStoreService.deleteInS3Async(fileToRemove.getFileIdInBucket());

        DeleteObjectResponse deleteResponse =
            deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Deleted file from S3: {}", deleteResponse);
    }

    @NonNull
    private FileInfoS3 saveNotizAttachmentInFilestorage(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfoS3 fileInfoS3 = FileInfoS3Mapper.mapFileUploadToFileInfoS3(fileUpload, FileTyp.NOTIZUPLOAD);
        // upload the file to S3
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileUpload, fileInfoS3);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.debug("Uploaded file to S3: {}", putObjectResponse);

        return fileInfoS3;
    }

    @NonNull
    public FileInfoS3 findUploadedFileInfoS3(@NonNull ID<Notiz> notizId, @NonNull ID<FileData> fileId) {
        return getById(notizId).getFiles().stream()
            .filter(fileData -> fileData.getId().equals(fileId))
            .findFirst()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found")))
            .getFileInfoBucket();
    }
}
