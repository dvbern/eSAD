package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.S3ZipWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
@Slf4j
public class PrintZipUtil {

    public static byte[] zipPrintAndAttachments(
        @NonNull PrintResultDTO printResult,
        @NonNull FileStoreService fileStoreService,
        @NonNull String attachmentPrefix,
        @NonNull List<Notiz> notizList
    )
        throws IOException {
        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(zipBaos)) {
            S3ZipWriter zipWriter = new S3ZipWriter(zos, fileStoreService);
            zipWriter.addEntry(printResult.getFileInfo().getFileName().getFileName(), printResult.getContent());

            Set<String> notizFolderNameList = new HashSet<>();
            for (Notiz notiz : notizList) {

                String folderName = calculateAutomaticFolderName(notizFolderNameList, notiz);

                for (FileData fileData : notiz.getFiles()) {
                    String entryName = attachmentPrefix + '/' + folderName + '/' + fileData.getCalculatedFileName();
                    zipWriter.addFromS3(entryName, fileData.getFileInfoBucket());
                }
            }
        }
        return zipBaos.toByteArray();
    }

    @NonNull
    private static String calculateAutomaticFolderName(@NonNull Set<String> existingFolders, @NonNull Notiz notiz) {
        int laufnummer = 0;
        String folderName;

        do {
            folderName = FileNameUtil.basicFolderName(notiz.getTimestampErstellt(), notiz.getTitel(), laufnummer);
            laufnummer++;
        } while (!existingFolders.add(folderName));

        return folderName;
    }
}
