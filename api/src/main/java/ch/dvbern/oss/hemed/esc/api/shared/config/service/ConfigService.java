/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.config.service;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
@Getter
@RequiredArgsConstructor
@Service
@Transactional(TxType.NOT_SUPPORTED)
public class ConfigService {

    @ConfigProperty(name = "app.deployment.app-project")
    String appProject;

    @ConfigProperty(name = "app.deployment.app-module")
    String appModule;

    @ConfigProperty(name = "app.deployment.environment")
    DeploymentEnvironment environment;

    @ConfigProperty(name = "app.deployment.instance")
    String instance;

    @ConfigProperty(name = "app.deployment.public-url")
    URL publicUrl;

    @ConfigProperty(name = "app.deployment.version")
    String version;

    @ConfigProperty(name = "app.deployment.client-auth-config.client-id")
    @IgnoreForIdClassTest
    String clientAuthConfigClientId;

    @ConfigProperty(name = "app.deployment.client-auth-config.realm")
    String clientAuthConfigRealm;

    @ConfigProperty(name = "app.deployment.client-auth-config.logout-url")
    URL clientAuthConfigLogoutUrl;

    @ConfigProperty(name = "app.deployment.client-auth-config.auth-url")
    URL clientAuthConfigAuthUrl;

    @ConfigProperty(name = "esc.benutzer.rolle.cache.timeout.minutes", defaultValue = "30")
    int benutzerRollenCacheTimeoutMinutes;

    /**
     * WARNING: to override this for your local dev envirionment do not set the property for all quarkus profield
     * (i.e. dev, test, prod) but only for the specific profile you are running (e.g. dev) because the tests
     * will likely fail otherwise.
     *
     * The best way is to write _DEV_ESC_TENANT_OVERRIDE=BS in your .env file*
     */
    @ConfigProperty(name = "esc.tenant.override")
    @Getter(AccessLevel.NONE)
    Optional<String> tenantOverride;

    @ConfigProperty(name = "app.allow-db-reset")
    boolean allowDbReset;

    @ConfigProperty(name = "app.allow-timetravel")
    boolean featureTimeTravelEndpointEnabled;

    @ConfigProperty(name = "app.version-filter-enabled")
    boolean versionFilterEnabled;

    // currently default is 60 days
    @ConfigProperty(name = "esc.scheduler.inactivate.benutzer.after.minutes.unused", defaultValue = "86400")
    int inactivateBenutzerAfterMinutesUnused;

    @ConfigProperty(name = "esc.scheduler.inactivate.kinder.after.inactive.months", defaultValue = "9")
    int inactivateKinderAfterInactiveMoths;

    @ConfigProperty(name = "esc.scheduler.inactivate.kinder.manual.inactivation.age", defaultValue = "16")
    int getInactiveKinderManualKinderInactivationAge;

    @ConfigProperty(name = "esc.s3.bucket-name")
    String s3BucketName;

    @ConfigProperty(name = "esc.s3.upload-timeout-seconds", defaultValue = "30s")
    Duration s3UploadTimeout;

    @ConfigProperty(name = "esc.s3.encryption-key.bs")
    Optional<String> s3EncryptionKeyBS;

    @ConfigProperty(name = "esc.s3.encryption-key.be")
    Optional<String> s3EncryptionKeyBE;

    @ConfigProperty(name = "esc.esca-sync.error-mail.enabled", defaultValue = "true")
    boolean escadaSyncErrorMailEnabled;

    @ConfigProperty(name = "esc.esca-sync.client.user", defaultValue = "escada-client@dvbern.ch")
    String escadaClientUser;

    @ConfigProperty(name = "esc.esca-sync.client.azp", defaultValue = "escada-client")
    String escadaAZP;

    @ConfigProperty(name = "esc.customer-admin-emails", defaultValue = "bs-esc@dvbern.ch")
    List<String> customerAdminEmails;

    @ConfigProperty(name = "esc.mail.overriding.recipients.enabled", defaultValue = "false")
    boolean mailOverridingRecipientsEnabled;

    @ConfigProperty(name = "esc.mail.overriding.recipients.address", defaultValue = "")
    List<String> mailOverridingRecipientsAddress;

    @ConfigProperty(name = "esc.quick-search.max-results", defaultValue = "40")
    int quickSearchMaxResults;

    @ConfigProperty(name = "esc.documentqueue.max.open.per.user", defaultValue = "10")
    int documentqueueMaxOpenPerUser;

    @ConfigProperty(name = "esc.documentqueue.max.life.time", defaultValue = "P7D")
    Duration documentqueueMaxLifeTime;

    /**
     * a list of file extensions that are allowed to be uploaded, can be extension or media type (e.g. .pdf or
     * application/pdf)
     */
    @ConfigProperty(name = "esc.upload.extension.whitelist",
        defaultValue = ".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.eml,.msg,.mbox,.jpg,.jpeg,.png,.heic,.webp")
    Set<String> uploadExtensionWhitelist;

    /**
     * Dieses File wurde von
     * <a href="https://swisspost.opendatasoft.com/explore/dataset/plz_verzeichnis_v2/table/">DiePost</a>
     * heruntergeladen. Die Spalten Geo Shape und Geokoordinaten wurden geleert
     */
    @ConfigProperty(name = "esc.plz-import.file-location", defaultValue = "/plz/plz_verzeichnis_v2_nogeo.csv")
    String plzImportFileLocation;

    /**
     * defines how many Anlasesse are loaded for the user on the dashboard
     */
    @ConfigProperty(name = "esc.dashboard.anlass.limit", defaultValue = "5")
    int dashboardAnlassLimit;

    /**
     * defines how many Aktivitaeten are loaded for the user on the dashboard
     */
    @ConfigProperty(name = "esc.dashboard.aktivitaet.history.limit", defaultValue = "20")
    int dashboardAktivitaetHistoryLimit;

    @NonNull
    public Optional<TenantEnum> getTenantOverride() {
        if (tenantOverride.isPresent()) {
            return tenantOverride.map(TenantEnum::valueOf);
        }
        return Optional.empty();
    }

    public void validateNotProdAndDbResetFeatureEnabled() {
        if (isStufeProd() || !isAllowDbReset()) {
            LOG.warn("Resetting database is not allowed in production environment or if feature is not enabled!");
            throw new AppFailureException(AppFailureMessage.internalError("DB Reset is forbidden"));
        }
    }

    public void validateNotProdAndTimeTravelFeatureEnabled() {
        if (isStufeProd() || !isFeatureTimeTravelEndpointEnabled()) {
            LOG.warn("Time travelling is not allowed in production environment or if feature is not enabled!");
            throw new AppFailureException(AppFailureMessage.internalError("Time travelling is forbidden"));
        }
    }

    public boolean isStufeProd() {
        var env = getEnvironment();
        return env.isProd();
    }

    @NonNull
    public Set<String> getFiletypeWhitelistedFileExtensionsForUploadType(@NonNull FileTyp notizupload) {
        if (notizupload == FileTyp.ESCADA_SYNC) {
            return Set.of("text/csv", "text/plain", "application/octet-stream");
        }
        if (notizupload == FileTyp.BRIEFVORLAGE) {
            return Set.of(
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return this.uploadExtensionWhitelist;
    }

    /**
     * this will return the Base64 encoded encryption key for the given tenant*
     */
    @NonNull
    public Optional<String> getS3EncryptionKey(@NonNull TenantEnum tenantEnum) {
        return switch (tenantEnum) {
            case TenantEnum.BS -> s3EncryptionKeyBS;
            case TenantEnum.BE -> s3EncryptionKeyBE;
            default -> throw new AppFailureException(AppFailureMessage.internalError("Unknown tenant: " + tenantEnum));
        };
    }
}
