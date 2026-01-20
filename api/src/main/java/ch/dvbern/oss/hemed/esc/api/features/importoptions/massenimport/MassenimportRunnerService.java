package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.MassenimportRollbackException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportAsyncParams;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportService.MASSENIMPORT_EVENT;

@Service
@Slf4j
@AllArgsConstructor
public class MassenimportRunnerService {

    private final TenantProducer tenantProducer;
    private final MassenimportService massenimportService;
    private final TL tl;

    @ActivateRequestContext
    @ConsumeEvent(value = MASSENIMPORT_EVENT, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void handleEvent(MassenimportAsyncParams params) {
        tenantProducer.setCurrentTenant(params.tenant());
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
