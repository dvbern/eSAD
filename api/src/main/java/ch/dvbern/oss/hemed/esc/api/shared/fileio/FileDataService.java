package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDataService {

    private final FileDataRepo fileDataRepo;

    @NonNull
    public FileInfo getFileInfoS3ByFileIdInBucket(@NonNull String fileIdInBucket) {
        return fileDataRepo.getByFileIdInBucket(fileIdInBucket).getFileInfoBucket();
    }
}
