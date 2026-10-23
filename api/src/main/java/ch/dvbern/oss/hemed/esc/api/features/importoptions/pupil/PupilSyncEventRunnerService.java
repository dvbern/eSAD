package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.PupilService.PUPIL_SYNC_EVENT_ADDRESS;

@Service
@RequiredArgsConstructor
public class PupilSyncEventRunnerService {

    /**
     * The Pupil import is fixed to WIN.
     */
    static final TenantEnum PUPIL_TENANT = TenantEnum.WIN;

    private final PerTenantRunnerService perTenantRunnerService;
    private final PupilService pupilService;

    /**
     * Runs in a request context of its own for {@link #PUPIL_TENANT}. The identity is set by the {@code @RunAs}
     * on {@link PupilService#handlePupilSyncEvent}, i.e. inside that context.
     */
    @ConsumeEvent(value = PUPIL_SYNC_EVENT_ADDRESS, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    public void handleEvent(@NonNull PupilSyncEvent pupilSyncEvent) {
        perTenantRunnerService.runForTenant(
            PUPIL_TENANT,
            () -> pupilService.handlePupilSyncEvent(pupilSyncEvent)
        );
    }
}
