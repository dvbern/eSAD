package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
class S3ClientProducerTest extends AbstractITest {


    private final S3ClientProducer s3ClientProducer;
    private final TenantProducer tenantProducer;
    private final ConfigService configService;

    private ConfigService configServiceMock;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        configServiceMock = Mockito.spy(configService);
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);
    }

    @Test
    void testGetS3AsyncClient_SingleBucket() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(true);

        // ACT
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantBS() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BS);

        // ACT
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantWIN() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.WIN);

        // ACT
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantBE() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        // ACT
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_DifferentTenantsProduceDifferentClients() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);

        // ACT
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        S3AsyncClient clientBS = s3ClientProducer.getS3AsyncClient();

        tenantProducer.setCurrentTenant(TenantEnum.WIN);
        S3AsyncClient clientWIN = s3ClientProducer.getS3AsyncClient();

        tenantProducer.setCurrentTenant(TenantEnum.BE);
        S3AsyncClient clientBE = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(clientBS).isNotNull();
        assertThat(clientWIN).isNotNull();
        assertThat(clientBE).isNotNull();

        assertThat(clientBS).isNotSameAs(clientWIN).isNotSameAs(clientBE);
        assertThat(clientWIN).isNotSameAs(clientBE).isNotSameAs(clientBS);
    }

    @Test
    void testGetS3AsyncClient_SingleBucketIsDifferentFromTenantClients() {
        // ARRANGE
        when(configServiceMock.isS3SingleBucket()).thenReturn(true);
        S3AsyncClient singleBucketClient = s3ClientProducer.getS3AsyncClient();

        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        S3AsyncClient clientBS = s3ClientProducer.getS3AsyncClient();

        // ASSERT
        assertThat(singleBucketClient).isNotSameAs(clientBS);
    }
}
