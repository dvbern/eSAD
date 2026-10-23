/*
 *
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext.ContextState;
import io.quarkus.arc.ManagedContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.MDC;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext.LOG_MDC_TENANT;

/**
 * The only supported way to run something for a tenant outside a regular REST request (scheduled jobs, event
 * consumers, health checks, ...).
 * <p>
 * Every tenant runs in a request context of its <b>own</b>. Request-scoped beans (and their fields) are created
 * for exactly one tenant: {@code S3AsyncClient} incl. credentials, the {@code S3RequestBuilder} with the tenant's
 * encryption key, the non-transactional Hibernate session, {@code AuthorizerService} caches, ... Reusing one
 * request context for several tenants would let all of them silently use the state of the first tenant.
 * <p>
 * Deliberately {@code @ApplicationScoped} and not a {@code @Service}, so the runner itself does not live in any
 * request context, and {@code NOT_SUPPORTED}, so no transaction (and its session bound to one tenant) leaks from
 * the caller into the tenant's task.
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@Transactional(TxType.NOT_SUPPORTED)
public class PerTenantRunnerService {

    private final TenantContext tenantContext;
    private final ConfigService configService;

    /**
     * All tenants active in the current environment. Single source of truth for everything that iterates tenants.
     */
    @NonNull
    public List<TenantEnum> getActiveTenants() {
        List<TenantEnum> activeTenants = new ArrayList<>();
        for (TenantEnum tenant : TenantEnum.values()) {
            if (tenant.isAktiv()
                && configService.isTenantEnabledForCurrentEnvironment(tenant)) {
                activeTenants.add(tenant);
            }
        }
        return activeTenants;
    }

    /**
     * Runs the task once per active tenant, each tenant in its own request context.
     * <p>
     * Unlike VacMe, a failing tenant does not abort the loop: the failure is logged with its stack trace, the
     * remaining tenants still run, and at the end one exception is thrown that names all failed tenants and
     * carries their failures as suppressed exceptions.
     */
    public void runForActiveTenants(@NonNull Runnable task) {
        List<TenantFailure> failures = new ArrayList<>();
        for (TenantEnum tenant : getActiveTenants()) {
            try {
                runForTenant(tenant, task);
            } catch (RuntimeException e) {
                LOG.error("ESC-PER-TENANT-RUNNER[{}]: failed", tenant, e);
                failures.add(new TenantFailure(tenant, e));
            }
        }
        if (!failures.isEmpty()) {
            IllegalStateException summary = new IllegalStateException(
                "ESC-PER-TENANT-RUNNER: task failed for tenants " + failedTenants(failures)
            );
            failures.forEach(failure -> summary.addSuppressed(failure.exception()));
            throw summary;
        }
    }

    public void runForTenant(@NonNull TenantEnum tenant, @NonNull Runnable task) {
        runForTenant(tenant, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Runs the task in a fresh request context with the given tenant set before the first query.
     * <p>
     * The request context is managed explicitly instead of via {@code @ActivateRequestContext}, because that
     * interceptor is a <b>no-op</b> when a context is already active on the calling thread (e.g. inside
     * {@code SmallRyeHealthHandlerBase}, {@code @Scheduled} or {@code @ConsumeEvent} methods). An already active
     * context is therefore suspended, the task gets a genuinely fresh one, and the caller's context (incl. its
     * tenant and MDC value) is restored afterwards.
     * <p>
     * The security identity is request-scoped as well: {@code @RunAs} must be placed on a method that is called
     * <b>inside</b> the task.
     */
    public <T> T runForTenant(@NonNull TenantEnum tenant, @NonNull Supplier<T> task) {
        ManagedContext requestContext = Arc.container()
            .requestContext();
        // null when no context is active on this thread, otherwise the caller's context which we suspend
        @Nullable
        ContextState callerState = requestContext.getStateIfActive();
        @Nullable
        String callerMdcTenant = MDC.get(LOG_MDC_TENANT);
        if (callerState != null) {
            requestContext.deactivate();
        }
        requestContext.activate();
        try {
            // must be set BEFORE the first query opens the EntityManager
            tenantContext.setCurrentTenant(tenant);
            return task.get();
        } finally {
            // destroys this tenant's request-scoped beans so the next tenant cannot inherit them
            requestContext.terminate();
            if (callerState != null) {
                requestContext.activate(callerState);
            }
            restoreMdc(callerMdcTenant);
        }
    }

    @NonNull
    private static List<TenantEnum> failedTenants(@NonNull List<TenantFailure> failures) {
        return failures.stream()
            .map(TenantFailure::tenant)
            .toList();
    }

    private static void restoreMdc(@Nullable String callerMdcTenant) {
        if (callerMdcTenant == null) {
            MDC.remove(LOG_MDC_TENANT);
        } else {
            MDC.put(LOG_MDC_TENANT, callerMdcTenant);
        }
    }

    private record TenantFailure(@NonNull TenantEnum tenant, @NonNull RuntimeException exception) {
    }
}
