package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.MassenimportRollbackException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportAsyncParams;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportService.MASSENIMPORT_EVENT;

@Service
@Slf4j
@AllArgsConstructor
public class MassenimportRunnerService {

    private final PerTenantRunnerService perTenantRunnerService;
    private final MassenimportService massenimportService;
    private final TL tl;

    /**
     * Runs in a request context of its own for the tenant of the import. The identity is set by the
     * {@code @RunAs} on {@link MassenimportService#processMassenimportFileForSchuleAndKlasse}, i.e. inside that
     * context.
     */
    @ConsumeEvent(value = MASSENIMPORT_EVENT, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    public void handleEvent(MassenimportAsyncParams params) {
        perTenantRunnerService.runForTenant(params.tenant(), () -> processMassenimport(params));
    }

    private void processMassenimport(MassenimportAsyncParams params) {
        try {
            massenimportService.processMassenimportFileForSchuleAndKlasse(
                params.sheet(),
                params.schuleID(),
                params.klasseID(),
                params.standardImport(),
                params.benutzer()
            );
        } catch (MassenimportRollbackException exception) {
            // we do not want to let this exception escape since it is part of the regular workflow. It is triggered
            // to rollback the transaction if there are problems with the supplied excel sheet
            LOG.info(
                "Massenimport: Excelimport was rolled back due to issues: {}",
                tl.translate(exception.getI18nMessage()));
        }
    }
}
