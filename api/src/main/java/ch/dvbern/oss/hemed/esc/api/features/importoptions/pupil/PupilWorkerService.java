package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Service
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
public class PupilWorkerService {

    private final PupilSyncEventService pupilSyncEventService;
    private final FileValidationService fileValidationService;
    private final EventBus eventBus;

    public void processPupilCsvUploadUsingEventBus(@NonNull FileUpload fileUpload) {
        // check the filetype
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.PUPIL_SYNC);
        // store the sync data in a file
        PupilSyncEvent pupilSyncEvent = pupilSyncEventService.savePupilSyncData(fileUpload);
        LOG.info("PUPIL: sending event for processing ");
        eventBus.requestAndForget(PupilService.PUPIL_SYNC_EVENT_ADDRESS, pupilSyncEvent);
    }
}
