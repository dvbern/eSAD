package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
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

    private final TenantProducer tenantProducer;
    private final ConfigService configService;

    @Inject
    public S3ClientProducer(
        @AmazonClient("bs") Instance<S3AsyncClient> s3AsyncClientBS,
        @AmazonClient("win") Instance<S3AsyncClient> s3AsyncClientWIN,
        @AmazonClient("be") Instance<S3AsyncClient> s3AsyncClientBE,
        @AmazonClient("default") Instance<S3AsyncClient> s3SingleBucketClient,
        TenantProducer tenantProducer,
        ConfigService configService

    ) {
        this.s3AsyncClientBS = s3AsyncClientBS;
        this.s3AsyncClientWIN = s3AsyncClientWIN;
        this.s3AsyncClientBE = s3AsyncClientBE;
        this.s3SingleBucketClient = s3SingleBucketClient;

        this.tenantProducer = tenantProducer;
        this.configService = configService;
    }

    @Produces
    public S3AsyncClient getS3AsyncClient() {

        //TODO ESC-1086: Team: remove after migrating buckets
        if (configService.isS3SingleBucket()) {
            return s3SingleBucketClient.get();
        }

        TenantEnum tenantEnum = tenantProducer.produceTenant();
        return switch (tenantEnum) {
            case BS -> s3AsyncClientBS.get();
            case WIN -> s3AsyncClientWIN.get();
            case BE -> s3AsyncClientBE.get();
            default -> throw new IllegalStateException("Unexpected value: " + tenantEnum);
        };
    }
}
