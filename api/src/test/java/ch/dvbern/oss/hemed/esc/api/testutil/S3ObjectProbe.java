package ch.dvbern.oss.hemed.esc.api.testutil;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import io.quarkiverse.amazon.common.AmazonClient;
import org.awaitility.Awaitility;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Looks into the test buckets without going through the tenant-dependent production code, so tests can check where a
 * file really is. All buckets live in the same LocalStack instance, one client reaches every one of them.
 */
@ApplicationScoped
public class S3ObjectProbe {

    private static final Duration DELETE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private final S3AsyncClient s3;
    private final String bucketBS;
    private final String bucketWIN;
    private final String bucketBE;
    private final String singleBucket;

    @Inject
    public S3ObjectProbe(
        @AmazonClient("bs") S3AsyncClient s3,
        @ConfigProperty(name = "esc.s3.bucket-name.bs") String bucketBS,
        @ConfigProperty(name = "esc.s3.bucket-name.win") String bucketWIN,
        @ConfigProperty(name = "esc.s3.bucket-name.be") String bucketBE,
        @ConfigProperty(name = "esc.s3.bucket-name") String singleBucket
    ) {
        this.s3 = s3;
        this.bucketBS = bucketBS;
        this.bucketWIN = bucketWIN;
        this.bucketBE = bucketBE;
        this.singleBucket = singleBucket;
    }

    @NonNull
    public String bucketOf(@NonNull TenantEnum tenant) {
        return switch (tenant) {
            case BS -> bucketBS;
            case WIN -> bucketWIN;
            case BE -> bucketBE;
            case DEFAULT -> throw new IllegalArgumentException("DEFAULT has no bucket");
        };
    }

    @NonNull
    public String singleBucket() {
        return singleBucket;
    }

    public boolean exists(@NonNull String bucket, @NonNull String key) {
        try {
            s3.headObject(builder -> builder.bucket(bucket).key(key)).join();
            return true;
        } catch (CompletionException e) {
            if (e.getCause() instanceof S3Exception s3Exception && s3Exception.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Every bucket the application knows about that currently holds the key.
     */
    @NonNull
    public List<String> bucketsContaining(@NonNull String key) {
        return Stream.of(bucketBS, bucketWIN, bucketBE, singleBucket)
            .distinct()
            .filter(bucket -> exists(bucket, key))
            .toList();
    }

    /**
     * For deletes that run on another thread (the transaction reaper): waits until the key is gone,
     * fails with ConditionTimeoutException after DELETE_TIMEOUT.
     */
    public void awaitGone(@NonNull String bucket, @NonNull String key) {
        Awaitility.await("S3 object %s/%s to be deleted".formatted(bucket, key))
            .pollDelay(Duration.ZERO)
            .pollInterval(POLL_INTERVAL)
            .atMost(DELETE_TIMEOUT)
            .until(() -> !exists(bucket, key));
    }
}
