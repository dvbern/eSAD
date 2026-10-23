package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.DbFlusher;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDataService {

    private final FileDataRepo fileDataRepo;
    private final FileStoreService fileStoreService;
    private final DbFlusher dbFlusher;

    @NonNull
    public FileInfo getFileInfoS3ByFileIdInBucket(@NonNull String fileIdInBucket) {
        return fileDataRepo.getByFileIdInBucket(fileIdInBucket).getFileInfoBucket();
    }

    public void deleteFileData(@NonNull List<FileData> filesToDelete, boolean deleteFileInS3) {
        fileDataRepo.flush();
        for (FileData fileToDelete : filesToDelete) {
            // Das FileData-Objekt aus der DB entfernen
            if (fileDataRepo.findById(fileToDelete.getId()).isPresent()) {
                fileDataRepo.remove(fileToDelete.getId());
                // flush to check if file can be deleted (no constraints violated), note that this does not run if the file was already removed due to cascades
                dbFlusher.flush();
            }

            if(deleteFileInS3){
                // only once the removal is committed: deleting now would lose the file if the transaction still rolls
                // back, e.g. when a later step of a Kind deletion fails
                fileStoreService.deleteInS3AfterCommit(fileToDelete.getFileInfoBucket().getFileIdInBucket());
            }
        }
    }

    @NonNull
    public FileData createFileData(@NonNull PrintResultDTO printResult, @NonNull FileTyp fileTyp, @NonNull String prefixFilename) {
        // automatisch generierter Name mit Prefix ergänzen
        CleanFileName cleanFileName = new CleanFileName(prefixFilename + '_' + printResult.getFileInfo().getFileName());
        FileInfo fileInfo = FileInfo.of(printResult.getFileInfo());
        fileInfo.setFileName(cleanFileName);
        // deleted again if the transaction does not commit
        PutObjectResponse putObjectResponse = fileStoreService.uploadToS3BoundToTransaction(
            fileInfo.getMediaType().toString(),
            printResult.getContent(),
            fileInfo
        );
        LOG.info(
            "Uploaded file to S3, fileIdInBucket: {}, putObjectResponse: {}",
            fileInfo.getFileIdInBucket(),
            putObjectResponse);
        return new FileData(fileTyp, fileInfo.getFileName(), fileInfo);
    }
}
