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

package ch.dvbern.dvbstarter.infrastructure.health.tenant;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;

import io.smallrye.health.api.Wellness;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Checks every active tenant in a request context of its own (via {@link PerTenantRunnerService}): the caller
 * ({@code SmallRyeHealthHandlerBase}) already runs inside an active request context, reusing it would bind all
 * request-scoped beans to the first tenant. A failure of one tenant does not prevent the others from being checked.
 * <p>
 * The set of active tenants is static for the lifetime of the application, so it is resolved once at construction.
 */
@ApplicationScoped
@Wellness
@Slf4j
public class TenantHealthCheck extends AbstractTenantHealthCheck implements HealthCheck {

    private final PerTenantRunnerService perTenantRunnerService;
    private final List<TenantInfo> tenantsToCheck;

    record TenantInfo(TenantEnum tenantEnum, ID<Benutzer> systemAdminId) {
    }

    record TenantOutcome(
        @NonNull TenantEnum tenant,
        @Nullable TenantHealthCheckResult result,
        @Nullable String error
    ) {
        boolean allChecksPassed() {
            return result != null
                && result.allChecksPassed();
        }
    }

    @Inject
    public TenantHealthCheck(
        @NonNull PerTenantRunnerService perTenantRunnerService,
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull BenutzerService benutzerService,
        @NonNull AnlassConfigService anlassConfigService,
        @NonNull SchedulerConfigService schedulerConfigService,
        @NonNull ConfigService configService,
        @NonNull AppClock appClock
    ) {
        super(
            tenantSettingsService,
            benutzerService,
            anlassConfigService,
            schedulerConfigService,
            configService,
            appClock
        );
        this.perTenantRunnerService = perTenantRunnerService;
        this.tenantsToCheck = perTenantRunnerService.getActiveTenants()
            .stream()
            .map(tenant -> new TenantInfo(tenant, Benutzer.getSystemAdminId(tenant)))
            .toList();
    }

    @Override
    public HealthCheckResponse call() {
        List<TenantOutcome> outcomes = tenantsToCheck.stream()
            .map(this::checkTenant)
            .toList();
        return buildResponse(outcomes);
    }

    @NonNull
    private TenantOutcome checkTenant(@NonNull TenantInfo tenantInfo) {
        TenantEnum tenant = tenantInfo.tenantEnum();
        try {
            TenantHealthCheckResult result = perTenantRunnerService.runForTenant(
                tenant,
                () -> runHealthChecks(tenantInfo)
            );
            return new TenantOutcome(tenant, result, null);
        } catch (RuntimeException e) {
            LOG.error("ESC-TENANT-READINESS-HEALTH-CHECK: Health check failed for tenant: {}", tenant, e);
            return new TenantOutcome(tenant, null, e.toString());
        }
    }

    @NonNull
    static HealthCheckResponse buildResponse(@NonNull List<TenantOutcome> outcomes) {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(NAME);
        if (outcomes.stream().allMatch(TenantOutcome::allChecksPassed)) {
            builder.up();
        } else {
            builder.down();
        }

        for (TenantOutcome outcome : outcomes) {
            String tenantPrefix = '[' + outcome.tenant().name() + "] ";
            TenantHealthCheckResult result = outcome.result();
            if (result != null) {
                builder
                    .withData(tenantPrefix + "tenantSettingsPresent", result.tenantSettingsPresent())
                    .withData(tenantPrefix + "tenantHeaderLogoPresent", result.tenantHeaderLogoPresent())
                    .withData(tenantPrefix + "systemAdminPresent", result.systemAdminPresent())
                    .withData(
                        tenantPrefix + "systemAdminNotReturnedByFindAll",
                        result.systemAdminNotReturnedByFindAll())
                    .withData(tenantPrefix + "anlassConfigPresent", result.anlassConfigPresent())
                    .withData(tenantPrefix + "schedulerConfigPresent", result.schedulerConfigPresent())
                    .withData(tenantPrefix + "s3EncryptionKeyPresent", result.s3EncryptionKeyPresent());
            }
            String error = outcome.error();
            if (error != null) {
                builder.withData(tenantPrefix + "error", error);
            }
            builder.withData(tenantPrefix + "allChecksPassed", outcome.allChecksPassed());
        }

        return builder.build();
    }
}
