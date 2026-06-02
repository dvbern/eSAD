package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.io.ByteArrayInputStream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class PreviewMapper {

    private final FileDataRepo fileDataRepo;
    private final FileStoreService fileStoreService;
    private final PreviewService previewService;

    @NonNull
    public PreviewResult generatePreview(@NonNull ID<FileData> fileDataId) {
        FileData fileData = fileDataRepo.getById(fileDataId);
        return generatePreview(fileData);

    }

    public PreviewResult generatePreview(@NonNull FileData fileData) {
        FileInfo fileInfo = fileData.getFileInfoBucket();
        String mimeType = fileInfo.getMediaType().toString();

        byte[] fileBytes = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
        return previewService.generatePreview(new ByteArrayInputStream(fileBytes), mimeType);
    }
}
