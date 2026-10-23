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
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import io.quarkus.vertx.ConsumeEvent;
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

    private final PerTenantRunnerService perTenantRunnerService;
    private final SuperAdminRunnerService superAdminRunnerService;

    /**
     * Runs in a request context of its own for the given tenant. The identity is set by the {@code @RunAs} on
     * {@link SuperAdminRunnerService#runReloadDuplikateJob(boolean)}, i.e. inside that context.
     */
    @ConsumeEvent(value = DUPLIKAT_RECALCULATION_EVENT, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    public void handleDuplikatRecalculationEvent(@NonNull TenantEnum tenantEnum) {
        LOG.info("ESC-RELOAD_DUPLIKATE: Triggered through event ");
        perTenantRunnerService.runForTenant(
            tenantEnum,
            () -> superAdminRunnerService.runReloadDuplikateJob(true)
        );
    }
}
