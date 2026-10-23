package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import java.util.List;

import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Request-scoped test bean that captures tenant-derived state the same way production services do (in fields).
 * Used to prove that every tenant gets its own request context.
 */
@RequestScoped
@RequiredArgsConstructor
public class TenantRequestScopeProbe {

    public static final String RUN_AS_USER = "per-tenant-runner-probe";

    @Getter
    private final S3AsyncClient s3AsyncClient;
    private final EntityManager entityManager;
    private final CurrentIdentityAssociation association;
    private final ConfigService configService;

    /**
     * The actual instance behind the client proxy.
     */
    @NonNull
    public TenantRequestScopeProbe self() {
        return this;
    }

    /**
     * Non-transactional query, i.e. served by the request-scoped session.
     */
    @NonNull
    public String currentDatabase() {
        return (String) entityManager.createNativeQuery("select current_database()")
            .getSingleResult();
    }

    /**
     * Access key of the S3 client captured in the field: wrong credentials caused the 403 of
     * ESC-FILESTORE-HEALTHCHECK.
     */
    @NonNull
    public String s3AccessKeyId() {
        return s3AsyncClient.serviceClientConfiguration()
            .credentialsProvider()
            .resolveIdentity()
            .join()
            .accessKeyId();
    }

    /**
     * Writes an object with the given key into the current tenant's bucket, using the captured S3 client.
     */
    public void putIntoTenantBucket(@NonNull String key) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(configService.getS3BucketName())
            .key(key)
            .build();
        s3AsyncClient.putObject(request, AsyncRequestBody.fromString(key))
            .join();
    }

    /**
     * Lists the current tenant's bucket the same way {@code FileStoreHealthCheckService} does.
     */
    @NonNull
    public List<String> listTenantBucket() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(configService.getS3BucketName())
            .build();
        return s3AsyncClient.listObjectsV2(request)
            .join()
            .contents()
            .stream()
            .map(S3Object::key)
            .toList();
    }

    @NonNull
    public String currentIdentityName() {
        return association.getIdentity()
            .getPrincipal()
            .getName();
    }

    @NonNull
    @RunAs(username = RUN_AS_USER, roles = { BerechtigungName.SYSTEM_ADMIN })
    public String currentIdentityNameRunAs() {
        return currentIdentityName();
    }
}
