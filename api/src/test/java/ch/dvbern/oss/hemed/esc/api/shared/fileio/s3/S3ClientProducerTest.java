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
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(true);

        // Act
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantBS() {
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BS);

        // Act
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantWIN() {
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.WIN);

        // Act
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_TenantBE() {
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        // Act
        S3AsyncClient client = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    void testGetS3AsyncClient_DifferentTenantsProduceDifferentClients() {
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(false);

        // Act
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        S3AsyncClient clientBS = s3ClientProducer.getS3AsyncClient();

        tenantProducer.setCurrentTenant(TenantEnum.WIN);
        S3AsyncClient clientWIN = s3ClientProducer.getS3AsyncClient();

        tenantProducer.setCurrentTenant(TenantEnum.BE);
        S3AsyncClient clientBE = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(clientBS).isNotNull();
        assertThat(clientWIN).isNotNull();
        assertThat(clientBE).isNotNull();

        assertThat(clientBS).isNotSameAs(clientWIN).isNotSameAs(clientBE);
        assertThat(clientWIN).isNotSameAs(clientBE).isNotSameAs(clientBS);
    }

    @Test
    void testGetS3AsyncClient_SingleBucketIsDifferentFromTenantClients() {
        // Arrange
        when(configServiceMock.isS3SingleBucket()).thenReturn(true);
        S3AsyncClient singleBucketClient = s3ClientProducer.getS3AsyncClient();

        when(configServiceMock.isS3SingleBucket()).thenReturn(false);
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        S3AsyncClient clientBS = s3ClientProducer.getS3AsyncClient();

        // Assert
        assertThat(singleBucketClient).isNotSameAs(clientBS);
    }
}
