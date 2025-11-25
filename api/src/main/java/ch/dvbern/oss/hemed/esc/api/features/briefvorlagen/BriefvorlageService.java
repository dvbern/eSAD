package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.*;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BriefvorlageService {

    private final BriefvorlageRepo briefvorlageRepo;
    private final FileValidationService fileValidationService;
    private final ConfigService configService;
    private final FileStoreService fileStoreService;

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
        briefvorlageRepo.remove(briefvorlageID);
    }

    @NonNull
    public FileData saveBriefvorlageAttachmentInFilestorage(@NonNull FileUpload fileUpload) {
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.BRIEFVORLAGE);

        // prepare the file info Embeddable
        FileInfoS3 fileInfoS3 = FileInfoS3Mapper.mapFileUploadToFileInfoS3(fileUpload, FileTyp.BRIEFVORLAGE);
        // upload the file to S3
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileUpload, fileInfoS3);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}", fileInfoS3.getFileIdInBucket(), putObjectResponse);

        return new FileData(FileTyp.BRIEFVORLAGE, new CleanFileName(fileUpload.fileName()), fileInfoS3);
    }

    public void removeBriefvorlageAttachmentInFilestorage(@NonNull FileData fileData) {
        Uni<DeleteObjectResponse> deleteObjectResponseUni =
            fileStoreService.deleteInS3Async(fileData.getFileInfoBucket().getFileIdInBucket());

        DeleteObjectResponse deleteResponse =
            deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Deleted file from S3: {}", deleteResponse);
    }

    @NonNull
    public FileInfoS3 findBriefvorlageFile(@NonNull ID<Briefvorlage> briefvorlageId) {
        return getById(briefvorlageId).getFile().getFileInfoBucket();
    }
}
