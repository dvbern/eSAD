package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.EscadaService.ESCADA_SYNC_EVENT_ADDRESS;

@Service
@RequiredArgsConstructor
public class EscadaSyncEventRunnerService {

    /**
     * The Escada import is fixed to BS.
     */
    static final TenantEnum ESCADA_TENANT = TenantEnum.BS;

    private final PerTenantRunnerService perTenantRunnerService;
    private final EscadaService escadaService;

    /**
     * Runs in a request context of its own for {@link #ESCADA_TENANT}. The identity is set by the {@code @RunAs}
     * on {@link EscadaService#handleEscadaSyncEvent}, i.e. inside that context.
     */
    @ConsumeEvent(value = ESCADA_SYNC_EVENT_ADDRESS, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    public void handleEvent(@NonNull EscadaSyncEvent escadaSyncEvent) {
        perTenantRunnerService.runForTenant(
            ESCADA_TENANT,
            () -> escadaService.handleEscadaSyncEvent(escadaSyncEvent)
        );
    }
}
