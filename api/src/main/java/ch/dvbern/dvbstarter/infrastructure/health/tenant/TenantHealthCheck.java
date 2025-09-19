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

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.pub.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfigService;
import io.smallrye.health.api.Wellness;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@RequestScoped
@Wellness
@Slf4j
public class TenantHealthCheck extends AbstractTenantHealthCheck implements HealthCheck {

    private final List<TenantInfos> tenantesToCheckHealthFor;

    record TenantInfos(TenantEnum tenantEnum, ID<Benutzer> systemAdminId) {}

    @Inject
    public TenantHealthCheck(
        @NonNull TenantProducer tenantProducer,
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull BenutzerService benutzerService,
        @NonNull BenutzerRepo benutzerRepo,
        @NonNull AnlassConfigService anlassConfigService,
        @NonNull SchedulerConfigService schedulerConfigService,
        @NonNull ConfigService configService
    ) {
        super(
            tenantProducer,
            tenantSettingsService,
            benutzerService,
            benutzerRepo,
            anlassConfigService,
            schedulerConfigService,
            configService
        );

        tenantesToCheckHealthFor = List.of(
            new TenantInfos(TenantEnum.BS, Benutzer.SYSTEM_ADMIN_BS_ID),
            new TenantInfos(TenantEnum.WIN, Benutzer.SYSTEM_ADMIN_WIN_ID)
        );
    }

    @Override
    public HealthCheckResponse call() {

        List<TenantHealthCheckResult> listOfResults = new ArrayList<>();
        for (TenantInfos tenantInfos : tenantesToCheckHealthFor) {
            TenantHealthCheckResult result = runHealthChecks(tenantInfos);
            listOfResults.add(result);
        }

        HealthCheckResponseBuilder builder = HealthCheckResponse.named(NAME);
        if (listOfResults.stream().allMatch(TenantHealthCheckResult::allChecksPassed)) {
            builder.up();
        } else {
            builder.down();

            // Add tenant-specific information when health checks fail
            for (TenantHealthCheckResult result : listOfResults) {
                String tenantPrefix = result.currentTenant().name() + "_";

                builder
                    .withData(tenantPrefix + "tenantSettingsPresent", result.tenantSettingsPresent())
                    .withData(tenantPrefix + "tenantHeaderLogoPresent", result.tenantHeaderLogoPresent())
                    .withData(tenantPrefix + "systemAdminPresent", result.systemAdminPresent())
                    .withData(tenantPrefix + "systemAdminNotReturnedByFindAll", result.systemAdminNotReturnedByFindAll())
                    .withData(tenantPrefix + "anlassConfigPresent", result.anlassConfigPresent())
                    .withData(tenantPrefix + "schedulerConfigPresent", result.schedulerConfigPresent())
                    .withData(tenantPrefix + "s3EncryptionKeyPresent", result.s3EncryptionKeyPresent())
                    .withData(tenantPrefix + "allChecksPassed", result.allChecksPassed());
            }
        }

        return builder.build();
    }
}
