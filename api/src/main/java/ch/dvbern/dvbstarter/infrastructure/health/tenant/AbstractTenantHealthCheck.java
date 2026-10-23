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

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.health.tenant.TenantHealthCheck.TenantInfo;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfig;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerTyp;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.health.HealthCheck;

@Slf4j
@NoArgsConstructor
abstract class AbstractTenantHealthCheck implements HealthCheck {

    protected static final String NAME = "TenantHealthCheck";

    record TenantHealthCheckResult(
        TenantEnum currentTenant,
        boolean tenantSettingsPresent,
        boolean tenantHeaderLogoPresent,
        boolean systemAdminPresent,
        boolean systemAdminNotReturnedByFindAll,
        boolean anlassConfigPresent,
        boolean schedulerConfigPresent,
        boolean s3EncryptionKeyPresent
    ) {
        public boolean allChecksPassed() {
            return tenantSettingsPresent
                && tenantHeaderLogoPresent
                && systemAdminPresent
                && systemAdminNotReturnedByFindAll
                && anlassConfigPresent
                && schedulerConfigPresent
                && s3EncryptionKeyPresent;
        }
    }

    private TenantSettingsService tenantSettingsService = null;
    private BenutzerService benutzerService = null;
    private AnlassConfigService anlassConfigService = null;
    private SchedulerConfigService schedulerConfigService = null;
    private ConfigService configService = null;
    private AppClock appClock = null;

    protected AbstractTenantHealthCheck(
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull BenutzerService benutzerService,
        @NonNull AnlassConfigService anlassConfigService,
        @NonNull SchedulerConfigService schedulerConfigService,
        @NonNull ConfigService configService,
        @NonNull AppClock appClock
    ) {
        this.tenantSettingsService = tenantSettingsService;
        this.benutzerService = benutzerService;
        this.anlassConfigService = anlassConfigService;
        this.schedulerConfigService = schedulerConfigService;
        this.configService = configService;
        this.appClock = appClock;
    }

    /**
     * Must be called inside the tenant's own request context, see
     * {@link ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService}.
     */
    @NonNull
    protected TenantHealthCheckResult runHealthChecks(@NonNull TenantInfo currentTenantInfo) {
        return new TenantHealthCheckResult(
            currentTenantInfo.tenantEnum(),
            areTenantSettingsPresent(currentTenantInfo.tenantEnum()),
            isTenantHeaderLogoPresent(currentTenantInfo.tenantEnum()),
            isSystemAdminPresent(currentTenantInfo),
            systemAdminShouldNotBeReturnedByFindAllBenutzer(currentTenantInfo),
            isAnlassConfigPresentForEveryAnlassTyp(currentTenantInfo),
            isSchedulerConfigPresentForEverySchedulerTyp(currentTenantInfo),
            isS3EncryptionKeyPresent(currentTenantInfo)
        );
    }

    private boolean areTenantSettingsPresent(@NonNull TenantEnum tenantEnum) {
        try {
            tenantSettingsService.getTenantSetting();
            return true;
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Tenant settings are not present for tenant: {}",
                tenantEnum, ex);
            return false;
        }
    }

    private boolean isTenantHeaderLogoPresent(@NonNull TenantEnum tenantEnum) {
        try (InputStream logoStream = BasePrinter.class.getResourceAsStream(
            BasePrinter.getTenantHeaderImage(tenantEnum))) {
            Objects.requireNonNull(logoStream);
            return true;
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Tenant header logo is not present for tenant: {}",
                tenantEnum, ex);
            return false;
        }
    }

    private boolean isSystemAdminPresent(@NonNull TenantInfo tenantInfo) {
        try {
            Benutzer systemAdmin = benutzerService.getById(tenantInfo.systemAdminId());
            if (systemAdmin.getSystemRollen() != null) {
                Set<Berechtigung> berechtigungen = systemAdmin.getSystemRollen().getBerechtigungen(tenantInfo.tenantEnum());
                return berechtigungen.stream()
                    .anyMatch(b -> Objects.equals(b.getBerechtigungName(), BerechtigungName.SYSTEM_ADMIN));
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: System admin is not present for tenant: {}",
                tenantInfo.tenantEnum(), ex);
        }
        return false;
    }

    private boolean systemAdminShouldNotBeReturnedByFindAllBenutzer(@NonNull TenantInfo tenantInfo) {
        try {
            Benutzer systemAdmin = benutzerService.getById(tenantInfo.systemAdminId());
            List<Benutzer> allBenutzer = benutzerService.findAllBenutzer();
            Validate.isTrue(!allBenutzer.isEmpty());
            Validate.isTrue(!allBenutzer.contains(systemAdmin));
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: System admin should not be returned by findAllBenutzer for "
                    + "tenant: {}",
                tenantInfo.tenantEnum(),
                ex);
            return false;
        }
        return true;
    }

    private boolean isAnlassConfigPresentForEveryAnlassTyp(@NonNull TenantInfo tenantInfo) {
        try {
            LocalDate stichtag = appClock.getToday();
            for (AnlassTyp anlassTyp : AnlassTyp.values()) {
                AnlassConfig anlassConfig = anlassConfigService.getByAnlassTypAndStichtag(anlassTyp, stichtag);
                Validate.isTrue(anlassConfig.getAnlassTyp() == anlassTyp);
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Anlass config is not for all types present for tenant: {}",
                tenantInfo.tenantEnum(), ex);
            return false;
        }
        return true;
    }

    private boolean isSchedulerConfigPresentForEverySchedulerTyp(@NonNull TenantInfo tenantInfo) {
        try {
            for (SchedulerTyp schedulerTyp : SchedulerTyp.values()) {
                SchedulerConfig schedulerConfig = schedulerConfigService.getSchedulerConfig(schedulerTyp);
                Validate.isTrue(schedulerConfig.getSchedulerTyp() == schedulerTyp);
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Scheduler config is not for all types present for tenant: {}",
                tenantInfo.tenantEnum(), ex);
            return false;
        }
        return true;
    }

    private boolean isS3EncryptionKeyPresent(@NonNull TenantInfo tenantInfo) {
        Optional<String> s3EncryptionKey = configService.getS3EncryptionKey();
        if (s3EncryptionKey.isEmpty() || s3EncryptionKey.get().isBlank()) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: S3 encryption key is not present for tenant: {}",
                tenantInfo.tenantEnum());
            return false;
        }
        return true;
    }
}
