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

import ch.dvbern.dvbstarter.infrastructure.health.tenant.TenantHealthCheck.TenantInfos;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.pub.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfig;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerTyp;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    private TenantProducer tenantProducer = null;
    private TenantSettingsService tenantSettingsService = null;
    private BenutzerService benutzerService = null;
    private BenutzerRepo benutzerRepo = null;
    private AnlassConfigService anlassConfigService = null;
    private SchedulerConfigService schedulerConfigService = null;
    private ConfigService configService = null;


    protected AbstractTenantHealthCheck(
        @NonNull TenantProducer tenantProducer,
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull BenutzerService benutzerService,
        @NonNull BenutzerRepo benutzerRepo,
        @NonNull AnlassConfigService anlassConfigService,
        @NonNull SchedulerConfigService schedulerConfigService,
        @NonNull ConfigService configService
    ) {
        this.tenantProducer = tenantProducer;
        this.tenantSettingsService = tenantSettingsService;
        this.benutzerService = benutzerService;
        this.benutzerRepo = benutzerRepo;
        this.anlassConfigService = anlassConfigService;
        this.schedulerConfigService = schedulerConfigService;
        this.configService = configService;
    }

    @NonNull
    protected TenantHealthCheckResult runHealthChecks(@NonNull TenantInfos currentTenantInfos) {
        tenantProducer.setCurrentTenant(currentTenantInfos.tenantEnum());
        return new TenantHealthCheckResult(
            currentTenantInfos.tenantEnum(),
            areTenantSettingsPresent(currentTenantInfos.tenantEnum()),
            isTenantHeaderLogoPresent(currentTenantInfos.tenantEnum()),
            isSystemAdminPresent(currentTenantInfos),
            systemAdminShouldNotBeReturnedByFindAllBenutzer(currentTenantInfos),
            isAnlassConfigPresentForEveryAnlassTyp(currentTenantInfos),
            isSchedulerConfigPresentForEverySchedulerTyp(currentTenantInfos),
            isS3EncryptionKeyPresent(currentTenantInfos)
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

    private boolean isSystemAdminPresent(@NonNull TenantInfos tenantInfos) {
        try {
            Benutzer systemAdmin = benutzerService.getById(tenantInfos.systemAdminId());
            Optional<Benutzer> systemAdminWithBerechtigungenOptional =
                benutzerRepo.getByBenutzernameWithBerechtigungen(systemAdmin.getEmail());
            Validate.isTrue(systemAdminWithBerechtigungenOptional.isPresent());
            Benutzer systemAdminWithBerechtigungen = systemAdminWithBerechtigungenOptional.get();
            if (systemAdminWithBerechtigungen.getId().equals(tenantInfos.systemAdminId())) {
                return systemAdminWithBerechtigungen.getBerechtigungen().stream()
                    .anyMatch(benutzerBerechtigung -> Objects.equals(
                        benutzerBerechtigung.getBerechtigung().getBerechtigungName(),
                        BerechtigungName.SYSTEM_ADMIN));
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: System admin is not present for tenant: {}",
                tenantInfos.tenantEnum(), ex);
        }
        return false;
    }

    private boolean systemAdminShouldNotBeReturnedByFindAllBenutzer(@NonNull TenantInfos tenantInfos) {
        try {
            Benutzer systemAdmin = benutzerService.getById(tenantInfos.systemAdminId());
            List<Benutzer> allBenutzer = benutzerService.findAllBenutzer();
            Validate.isTrue(!allBenutzer.isEmpty());
            Validate.isTrue(!allBenutzer.contains(systemAdmin));
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: System admin should not be returned by findAllBenutzer for "
                    + "tenant: {}",
                tenantInfos.tenantEnum(),
                ex);
            return false;
        }
        return true;
    }

    private boolean isAnlassConfigPresentForEveryAnlassTyp(@NonNull TenantInfos tenantInfos) {
        try {
            LocalDate stichtag = LocalDate.now();
            for (AnlassTyp anlassTyp : AnlassTyp.values()) {
                AnlassConfig anlassConfig = anlassConfigService.getByAnlassTypAndStichtag(anlassTyp, stichtag);
                Validate.isTrue(anlassConfig.getAnlassTyp() == anlassTyp);
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Anlass config is not for all types present for tenant: {}",
                tenantInfos.tenantEnum(), ex);
            return false;
        }
        return true;
    }

    private boolean isSchedulerConfigPresentForEverySchedulerTyp(@NonNull TenantInfos tenantInfos) {
        try {
            for (SchedulerTyp schedulerTyp : SchedulerTyp.values()) {
                SchedulerConfig schedulerConfig = schedulerConfigService.getSchedulerConfig(schedulerTyp);
                Validate.isTrue(schedulerConfig.getSchedulerTyp() == schedulerTyp);
            }
        } catch (Exception ex) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: Scheduler config is not for all types present for tenant: {}",
                tenantInfos.tenantEnum(), ex);
            return false;
        }
        return true;
    }

    private boolean isS3EncryptionKeyPresent(@NonNull TenantInfos tenantInfos) {
        Optional<String> s3EncryptionKey = configService.getS3EncryptionKey();
        if (s3EncryptionKey.isEmpty() || s3EncryptionKey.get().isBlank()) {
            LOG.error(
                "ESC-TENANT-READINESS-HEALTH-CHECK: S3 encryption key is not present for tenant: {}",
                tenantInfos.tenantEnum());
            return false;
        }
        return true;
    }
}
