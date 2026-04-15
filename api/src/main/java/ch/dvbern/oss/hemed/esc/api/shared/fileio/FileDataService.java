package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.DbFlusher;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDataService {

    private final FileDataRepo fileDataRepo;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final DbFlusher dbFlusher;

    @NonNull
    public FileInfo getFileInfoS3ByFileIdInBucket(@NonNull String fileIdInBucket) {
        return fileDataRepo.getByFileIdInBucket(fileIdInBucket).getFileInfoBucket();
    }

    public void deleteFileData(@NonNull List<FileData> filesToDelete, boolean deleteFileInS3) {
        for (FileData fileToDelete : filesToDelete) {
            // Das FileData-Objekt aus der DB entfernen
            if (fileDataRepo.findById(fileToDelete.getId()).isPresent()) {
                fileDataRepo.remove(fileToDelete.getId());
                dbFlusher.flush(); // flush to check if file can be deleted (no constraints violated)
            }

            if(deleteFileInS3){
                // Das File in S3 entfernen
                Uni<DeleteObjectResponse> deleteObjectResponseUni =
                    fileStoreService.deleteInS3Async(fileToDelete.getFileInfoBucket().getFileIdInBucket());
                DeleteObjectResponse deleteResponse =
                    deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
                LOG.info("Deleted file from S3: {}", deleteResponse);
            }
        }
    }

    @NonNull
    public FileData createFileData(@NonNull PrintResultDTO printResult, @NonNull FileTyp fileTyp, @NonNull String prefixFilename) {
        // automatisch generierter Name mit Prefix ergänzen
        CleanFileName cleanFileName = new CleanFileName(prefixFilename + '_' + printResult.getFileInfo().getFileName());
        FileInfo fileInfo = FileInfo.of(printResult.getFileInfo());
        fileInfo.setFileName(cleanFileName);
        Uni<PutObjectResponse> putObjectResponseUni =
            fileStoreService.uploadToS3Async(fileInfo.getMediaType().toString(), printResult.getContent(), fileInfo);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info(
            "Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}",
            fileInfo.getFileIdInBucket(),
            putObjectResponse);
        return new FileData(fileTyp, fileInfo.getFileName(), fileInfo);
    }
}
