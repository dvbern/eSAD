package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStoreHealthCheckService {

    private final S3AsyncClient s3;
    private final ConfigService configService;
    private final TenantProducer tenantProducer;
    private final FileDataRepo fileDataRepo;
    private final TenantSettingsService tenantSettingsService;
    private final FileStoreDiffWhitelistLoader whitelistLoader;
    private final FileStoreHealthCheckMailService fileStoreHealthCheckMailService;

    @Transactional(TxType.NOT_SUPPORTED)
    public void runForCurrentTenant() {
        TenantEnum tenant = tenantProducer.produceTenant();
        LOG.info("ESC-FILESTORE-HEALTHCHECK[{}]: starting", tenant);
        String bucket = configService.getS3BucketName();
        Set<String> s3Keys = listAllS3Keys(bucket);
        Set<String> dbKeys = new HashSet<>(fileDataRepo.findAllFileIdsInBucket());
        String supportEmail = tenantSettingsService.getTenantSetting().getSupportEmail();
        List<String> recipients = List.of(Objects.requireNonNull(supportEmail, "Support email is required to send report"));
        performDiffAndNotify(s3Keys, dbKeys, tenant.name(), recipients);
        LOG.info("ESC-FILESTORE-HEALTHCHECK[{}]: done", tenant);
    }

    private void performDiffAndNotify(
        @NonNull Set<String> s3Keys,
        @NonNull Set<String> dbKeys,
        @NonNull String tenantLabel,
        @NonNull List<String> recipients
    ) {
        Set<String> whitelist = whitelistLoader.load(
            configService.getFilestoreHealthcheckWhitelistFile().orElse(null)
        );

        Set<String> dbOnlyFull = minus(dbKeys, s3Keys, whitelist);
        Set<String> s3OnlyFull = minus(s3Keys, dbKeys, whitelist);
        int dbOnlyTotal = dbOnlyFull.size();
        int s3OnlyTotal = s3OnlyFull.size();

        if (dbOnlyTotal == 0 && s3OnlyTotal == 0) {
            LOG.info("ESC-FILESTORE-HEALTHCHECK[{}]: no drift detected", tenantLabel);
            return;
        }

        LOG.warn(
            "ESC-FILESTORE-HEALTHCHECK[{}]: drift detected: {} in DB only, {} in S3 only",
            tenantLabel, dbOnlyTotal, s3OnlyTotal);

        int max = configService.getFilestoreHealthcheckReportMaxEntries();
        FileStoreDiffReport report = new FileStoreDiffReport(
            truncate(dbOnlyFull, max),
            truncate(s3OnlyFull, max),
            dbOnlyTotal,
            s3OnlyTotal
        );
        fileStoreHealthCheckMailService.sendReport(report, tenantLabel, recipients);
    }

    @NonNull
    private Set<String> listAllS3Keys(@NonNull String bucket) {
        // AWS SDK delivers subscriber callbacks on its own thread pool, so the set must be thread-safe
        Set<String> keys = ConcurrentHashMap.newKeySet();
        ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucket).build();
        try {
            s3.listObjectsV2Paginator(req)
                .contents()
                .subscribe(obj -> keys.add(obj.key()))
                .join();
        } catch (Exception e) {
            throw new IllegalStateException(
                "ESC-FILESTORE-HEALTHCHECK: failed to list S3 keys for bucket " + bucket, e);
        }
        LOG.info("ESC-FILESTORE-HEALTHCHECK: listed {} keys from bucket {}", keys.size(), bucket);
        return keys;
    }

    @NonNull
    private Set<String> minus(@NonNull Set<String> base, @NonNull Set<String> toRemove, @NonNull Set<String> whitelist) {
        Set<String> result = new HashSet<>(base);
        result.removeAll(toRemove);
        result.removeAll(whitelist);
        return result;
    }

    @NonNull
    private Set<String> truncate(@NonNull Set<String> set, int max) {
        if (set.size() <= max) {
            return set;
        }
        return set.stream().sorted().limit(max).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
