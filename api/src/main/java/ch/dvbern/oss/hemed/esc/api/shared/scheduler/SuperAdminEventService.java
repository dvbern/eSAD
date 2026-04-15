/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperAdminEventService {

    public static final String DUPLIKAT_RECALCULATION_EVENT = "duplikat_recalculation_event";

    private final TenantProducer tenantProducer;
    private final SuperAdminRunnerService superAdminRunnerService;

    @ActivateRequestContext
    @ConsumeEvent(value = DUPLIKAT_RECALCULATION_EVENT, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "dev-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void handleDuplikatRecalculationEvent(@NonNull TenantEnum tenantEnum) {
        LOG.info("ESC-RELOAD_DUPLIKATE: Triggered through event ");
        tenantProducer.setCurrentTenant(tenantEnum);
        superAdminRunnerService.runReloadDuplikateJob(true);
    }
}
