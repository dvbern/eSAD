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
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ClientAccountConfig.SvcAccountClientInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

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

    public static final String UNKNOWN_TENANT = "Unknown tenant: ";
    public static final String NOT_CONFIGURED_FOR_TENANT = "Configuration not supported for tenant: ";

    private final TenantProducer tenantProducer;

    @ConfigProperty(name = "app.deployment.app-project")
    String appProject;

    @ConfigProperty(name = "app.deployment.app-module")
    String appModule;

    @ConfigProperty(name = "app.deployment.environment")
    DeploymentEnvironment environment;

    @ConfigProperty(name = "app.deployment.instance")
    String instance;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "app.deployment.public-url.bs")
    URL publicUrlBS;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "app.deployment.public-url.win")
    URL publicUrlWIN;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "app.deployment.public-url.be")
    URL publicUrlBE;

    @ConfigProperty(name = "app.deployment.version")
    String version;

    @ConfigProperty(name = "app.deployment.client-auth-config.client-id")
    @IgnoreForIdClassTest
    String clientAuthConfigClientId;

    @ConfigProperty(name = "app.deployment.client-auth-config.realm")
    String clientAuthConfigRealm;

    @ConfigProperty(name = "app.deployment.client-auth-config.logout-url")
    Optional<URL> clientAuthConfigLogoutUrl;

    @ConfigProperty(name = "app.deployment.client-auth-config.auth-url")
    URL clientAuthConfigAuthUrl;

    @ConfigProperty(name = "esc.benutzer.rolle.cache.timeout.minutes", defaultValue = "30")
    int benutzerRollenCacheTimeoutMinutes;

    @ConfigProperty(name = "app.allow-db-reset")
    boolean allowDbReset;

    @ConfigProperty(name = "app.allow-timetravel")
    boolean featureTimeTravelEndpointEnabled;

    @ConfigProperty(name = "app.allow-configprint")
    boolean featureConfigPrintEnabled;

    @ConfigProperty(name = "app.version-filter-enabled")
    boolean versionFilterEnabled;

    // currently default is 60 days
    @ConfigProperty(name = "esc.scheduler.inactivate.benutzer.after.minutes.unused", defaultValue = "86400")
    int inactivateBenutzerAfterMinutesUnused;

    @ConfigProperty(name = "esc.scheduler.inactivate.kinder.after.inactive.months", defaultValue = "9")
    int inactivateKinderAfterInactiveMoths;

    @ConfigProperty(name = "esc.scheduler.inactivate.kinder.manual.inactivation.age", defaultValue = "16")
    int getInactiveKinderManualKinderInactivationAge;

    @ConfigProperty(name = "esc.scheduler.deactivate.impfstoffchargen.unused.months", defaultValue = "6")
    int deactivateImpfstoffChargenAfterUnusedMonths;

    @ConfigProperty(name = "esc.scheduler.delete.impfstoffchargen.unreferenced.months", defaultValue = "6")
    int deleteImpfstoffChargenAfterUnreferencedMonths;

    //TODO ESC-1086: Team: remove after migrating buckets
    @ConfigProperty(name = "esc.s3.use.single.bucket", defaultValue = "false")
    boolean s3SingleBucket;

    @Getter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "esc.s3.bucket-name")
    String s3BucketNameSingle;

    @Getter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "esc.s3.bucket-name.bs")
    String s3BucketNameBS;

    @Getter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "esc.s3.bucket-name.win")
    String s3BucketNameWIN;

    @Getter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "esc.s3.bucket-name.be")
    String s3BucketNameBE;

    @ConfigProperty(name = "esc.s3.upload-timeout-seconds", defaultValue = "30s")
    Duration s3UploadTimeout;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "esc.s3.encryption-key.bs")
    Optional<String> s3EncryptionKeyBS;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "esc.s3.encryption-key.win")
    Optional<String> s3EncryptionKeyWIN;

    @Getter(AccessLevel.NONE)
    @ConfigProperty(name = "esc.s3.encryption-key.be")
    Optional<String> s3EncryptionKeyBE;

    @ConfigProperty(name = "esc.esca-sync.error-mail.enabled", defaultValue = "true")
    boolean escadaSyncErrorMailEnabled;

    @ConfigProperty(name = "esc.esca-sync.client.user", defaultValue = "escada-client@dvbern.ch")
    String escadaClientUser;

    @ConfigProperty(name = "esc.esca-sync.client.azp", defaultValue = "escada-client")
    String escadaAZP;

    @ConfigProperty(name = "esc.esca-sync.log.redact.personalinfo", defaultValue = "true")
    boolean externalImportRedactPersonalInfo;

    @ConfigProperty(name = "esc.pupil-sync.error-mail.enabled", defaultValue = "true")
    boolean pupilSyncErrorMailEnabled;

    @ConfigProperty(name = "esc.pupil-sync.client.user", defaultValue = "pupil-client@dvbern.ch")
    String pupilClientUser;

    @ConfigProperty(name = "esc.pupil-sync.client.azp", defaultValue = "pupil-client")
    String pupilAZP;

    @ConfigProperty(name = "esc.file-ingress.client.user", defaultValue = "file-ingress-client@dvbern.ch")
    String fileIngressClientUser;

    @ConfigProperty(name = "esc.file-ingress.client.azp", defaultValue = "file-ingress-client")
    String fileIngressAZP;

    @ConfigProperty(name = "esc.hellodata-export.client.user", defaultValue = "service-account-hello-data-client")
    String helloDataClientUser;

    @ConfigProperty(name = "esc.hellodata-export.client.azp", defaultValue = "hello-data-client")
    String helloDataAZP;

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

    @ConfigProperty(name = "esc.dossierqueue.max.life.time", defaultValue = "P7D")
    Duration dossierQueueMaxLifeTime;

    /**
     * a list of file extensions that are allowed to be uploaded, can be extension or media type (e.g. .pdf or
     * application/pdf)
     * !!!IMPORTANT!!!
     * Keep list in frontend in sync, as we do not sync the config to the client yet
     * See usages of feat-upload-table and feat-file-drop to cover all upload use cases
     */
    @ConfigProperty(name = "esc.upload.extension.whitelist",
        defaultValue = ".pdf,.docx,.xlsx,.pptx,.eml,.msg,.jpg,.jpeg,.png,.heic,.webp")
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

    @ConfigProperty(name = "esc.feature.optimistic.locking.disabled", defaultValue = "false")
    boolean featureOptimisticLockingDisabled;

    // we 'reuse' the quarkus property to enable of disable the tenants database logically in esc
    @ConfigProperty(name = "quarkus.datasource.bs.active")
    Optional<Boolean> tenantBsActiveForCurrentEnvironment;

    // we 'reuse' the quarkus property to enable of disable the tenants database logically in esc
    @ConfigProperty(name = "quarkus.datasource.win.active")
    Optional<Boolean> tenantWinActiveForCurrentEnvironment;

    // we 'reuse' the quarkus property to enable of disable the tenants database logically in esc
    @ConfigProperty(name = "quarkus.datasource.be.active")
    Optional<Boolean> tenantBeActiveForCurrentEnvironment;

    // Anzahl Monate nach welchen Kinder gelöscht werden nachdem sie archiviert worden sind.
    @ConfigProperty(name = "esc.archive.months.before.deletion", defaultValue = "6")
    int archiveMonthsBeforeDeletion;

    /**
     * Soft cap for the number of archive documents bundled into a single zip by the bulk download.
     * <p>
     * The zip is written to a temp file, so heap is not the limiting factor. However the transaction limit is 60s
     * per document whe have to do: the id lookup, the per-document, authorization and the zip loop
     * Suspending per S3 call does not pause that clock.
     */
    @ConfigProperty(name = "esc.archive.bulk-download.max-files", defaultValue = "600")
    int archiveBulkDownloadMaxFiles;

    @ConfigProperty(name = "esc.tmp.dir")
    Path tmpDir;

    @ConfigProperty(name = "esc.filestore.healthcheck.enabled", defaultValue = "true")
    boolean filestoreHealthcheckEnabled;

    @ConfigProperty(name = "esc.filestore.healthcheck.whitelist-file")
    Optional<Path> filestoreHealthcheckWhitelistFile;

    @ConfigProperty(name = "esc.filestore.healthcheck.report-max-entries", defaultValue = "500")
    int filestoreHealthcheckReportMaxEntries;

    /** Document360 JWT SSO Client ID from Settings → Authentication. */
    @ConfigProperty(name = "esc.document360.client-id")
    @IgnoreForIdClassTest
    String document360ClientId;

    /** Document360 JWT SSO Client Secret. Inject via {@code ESC_DOCUMENT360_CLIENT_SECRET}; never commit it. */
    @ConfigProperty(name = "esc.document360.client-secret")
    String document360ClientSecret;

    /**
     * Full Document360 Identity URL for {@code POST /jwt/generateCode}.
     * Also bound to {@code quarkus.rest-client.document360-api.url}.
     */
    @ConfigProperty(name = "esc.document360.code-generation-url", defaultValue = "")
    URL document360CodeGenerationUrl;

    /** Knowledge-base login URL that accepts {@code ?code=} after generateCode succeeds. */
    @ConfigProperty(name = "esc.document360.kb-login-url")
    URL document360KbLoginUrl;


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

    /**
     * The machine-to-machine service-account clients, assembled from the individual
     * {@code esc.*.client.*} properties. Each entry maps a client's azp + username to the single
     * fixed {@link Berechtigung} it should receive. The roles augmentation uses this to grant
     * these clients their role before the normal (token- or database-based) FachRolle resolution,
     * independent of the tenant.
     */
    @NonNull
    public ClientAccountConfig getClientAccounts() {
        List<SvcAccountClientInfo> entries = List.of(
            new SvcAccountClientInfo(escadaAZP, escadaClientUser, Berechtigung.ESCADA_PUSH),
            new SvcAccountClientInfo(fileIngressAZP, fileIngressClientUser, Berechtigung.FILE_INGRESS_PUSH),
            new SvcAccountClientInfo(pupilAZP, pupilClientUser, Berechtigung.PUPIL_PUSH),
            new SvcAccountClientInfo(helloDataAZP, helloDataClientUser, Berechtigung.HELLODATA_EXPORT_LESEN)
        );
        return () -> entries;
    }

    @NonNull
    public Set<String> getFileTypeWhitelistedFileExtensionsForUploadType(@NonNull FileTyp fileTyp) {
        if (fileTyp == FileTyp.ESCADA_SYNC) {
            return Set.of("text/csv", "text/plain");
        }
        if (fileTyp == FileTyp.PUPIL_SYNC) {
            return Set.of("text/csv", "text/plain");
        }
        if (fileTyp == FileTyp.BRIEFVORLAGE) {
            // According to MDN, this is mime type is for .docx files
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types
            return Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return this.uploadExtensionWhitelist;
    }

    /**
     * this will return the Base64 encoded encryption key for the given tenant*
     */
    @NonNull
    public Optional<String> getS3EncryptionKey() {
        TenantEnum tenantEnum = getTenantProducer().produceTenant();
        return switch (tenantEnum) {
            case TenantEnum.BS -> s3EncryptionKeyBS;
            case TenantEnum.WIN -> s3EncryptionKeyWIN;
            case TenantEnum.BE -> s3EncryptionKeyBE;
            default -> throw new AppFailureException(AppFailureMessage.internalError(UNKNOWN_TENANT + tenantEnum));
        };
    }

    public boolean isTenantEnabledForCurrentEnvironment(@NonNull TenantEnum tenantEnum) {
        return switch (tenantEnum) {
            case TenantEnum.BS -> tenantBsActiveForCurrentEnvironment.orElse(Boolean.FALSE);
            case TenantEnum.WIN -> tenantWinActiveForCurrentEnvironment.orElse(Boolean.FALSE);
            case TenantEnum.BE -> tenantBeActiveForCurrentEnvironment.orElse(Boolean.FALSE);
            default -> throw new AppFailureException(AppFailureMessage.internalError(UNKNOWN_TENANT + tenantEnum));
        };
    }

    @NonNull
    public URL getPublicUrl() {
        TenantEnum tenantEnum = getTenantProducer().produceTenant();
        return switch (tenantEnum) {
            case TenantEnum.BS -> publicUrlBS;
            case TenantEnum.WIN -> publicUrlWIN;
            case TenantEnum.BE -> publicUrlBE;
            default -> throw new AppFailureException(AppFailureMessage.internalError(UNKNOWN_TENANT + tenantEnum));
        };
    }

    @NonNull
    public String getS3BucketName() {
        // return single bucket name if single bucket mode is enabled
        if (isS3SingleBucket()) {
            return this.getS3BucketNameSingle();
        }

        TenantEnum tenantEnum = getTenantProducer().produceTenant();
        return switch (tenantEnum) {
            case TenantEnum.BS -> getS3BucketNameBS();
            case TenantEnum.WIN -> getS3BucketNameWIN();
            case TenantEnum.BE -> getS3BucketNameBE();
            default -> throw new AppFailureException(AppFailureMessage.internalError(UNKNOWN_TENANT + tenantEnum));
        };
    }
}
