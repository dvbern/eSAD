package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BriefvorlageService {

    private final BriefvorlageRepo briefvorlageRepo;
    private final FileValidationService fileValidationService;
    private final FileStoreService fileStoreService;
    private final FileDataService fileDataService;

    @NonNull
    public Briefvorlage getById(@NonNull ID<Briefvorlage> briefvorlageID) {
        return briefvorlageRepo.getById(briefvorlageID);
    }

    @NonNull
    public List<Briefvorlage> findAll() {
        return briefvorlageRepo.findAll();
    }

    @NonNull
    public Briefvorlage persist(@NonNull Briefvorlage briefvorlage) {
        briefvorlageRepo.persist(briefvorlage);
        return briefvorlage;
    }

    @NonNull
    public Briefvorlage persistWithOptimisticLocking(@NonNull Briefvorlage briefvorlage, @NonNull Long versionFromFrontend) {
        briefvorlageRepo.persistWithOptimisticLocking(briefvorlage, versionFromFrontend);
        return briefvorlage;
    }

    public void remove(@NonNull ID<Briefvorlage> briefvorlageID) {
        Briefvorlage briefvorlage = getById(briefvorlageID);
        // Die Vorlage entfernen
        briefvorlageRepo.remove(briefvorlageID);
        // Das File in S3 entfernen
        fileDataService.deleteFileData(List.of(briefvorlage.getFile()), true);
    }

    @NonNull
    public FileData saveBriefvorlageAttachmentInFilestorage(@NonNull FileUpload fileUpload) {
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.BRIEFVORLAGE);

        // prepare the file info Embeddable
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.BRIEFVORLAGE);
        // upload the file to S3; it is deleted again if the transaction does not commit
        PutObjectResponse putObjectResponse = fileStoreService.uploadToS3BoundToTransaction(
            fileUpload,
            fileInfo
        );
        LOG.info("Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}", fileInfo.getFileIdInBucket(), putObjectResponse);

        return new FileData(FileTyp.BRIEFVORLAGE, new CleanFileName(fileUpload.fileName()), fileInfo);
    }

    public void removeBriefvorlageAttachmentInFilestorage(@NonNull FileData fileData) {
        // only once the replacement is committed: a failed update (e.g. a stale version) keeps the old file
        fileStoreService.deleteInS3AfterCommit(fileData.getFileInfoBucket().getFileIdInBucket());
    }

    @NonNull
    public FileInfo findBriefvorlageFile(@NonNull ID<Briefvorlage> briefvorlageId) {
        return getById(briefvorlageId).getFile().getFileInfoBucket();
    }
}
