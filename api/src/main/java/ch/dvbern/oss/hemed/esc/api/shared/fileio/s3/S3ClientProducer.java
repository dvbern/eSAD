package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkiverse.amazon.common.AmazonClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RequestScoped
public class S3ClientProducer {

    private final Instance<S3AsyncClient> s3AsyncClientBS;
    private final Instance<S3AsyncClient> s3AsyncClientBE;
    private final Instance<S3AsyncClient> s3AsyncClientWIN;
    private final Instance<S3AsyncClient> s3SingleBucketClient;

    private final TenantContext tenantContext;
    private final ConfigService configService;

    @Inject
    public S3ClientProducer(
        @AmazonClient("bs") Instance<S3AsyncClient> s3AsyncClientBS,
        @AmazonClient("win") Instance<S3AsyncClient> s3AsyncClientWIN,
        @AmazonClient("be") Instance<S3AsyncClient> s3AsyncClientBE,
        @AmazonClient("default") Instance<S3AsyncClient> s3SingleBucketClient,
        TenantContext tenantContext,
        ConfigService configService

    ) {
        this.s3AsyncClientBS = s3AsyncClientBS;
        this.s3AsyncClientWIN = s3AsyncClientWIN;
        this.s3AsyncClientBE = s3AsyncClientBE;
        this.s3SingleBucketClient = s3SingleBucketClient;

        this.tenantContext = tenantContext;
        this.configService = configService;
    }

    /**
     * The produced client must stay {@code @Dependent} (no scope on this method): the tenant is resolved on every
     * injection. Consumers hold the client in their fields and must therefore not live longer than the tenant's
     * request context, i.e. never inject it into {@code @ApplicationScoped}/{@code @Singleton} beans and never
     * switch the tenant inside a request context (use
     * {@link ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService}).
     */
    @Produces
    public S3AsyncClient getS3AsyncClient() {

        //TODO ESC-1086: Team: remove after migrating buckets
        if (configService.isS3SingleBucket()) {
            return s3SingleBucketClient.get();
        }

        TenantEnum tenantEnum = tenantContext.get();
        return switch (tenantEnum) {
            case BS -> s3AsyncClientBS.get();
            case WIN -> s3AsyncClientWIN.get();
            case BE -> s3AsyncClientBE.get();
            default -> throw new IllegalStateException("Unexpected value: " + tenantEnum);
        };
    }
}
