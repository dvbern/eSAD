package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseInfoRepo;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.liquibase.LiquibaseFactory;
import liquibase.Liquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseResourceTest {

    private final LiquibaseFactory liquibaseFactory = Mockito.mock(LiquibaseFactory.class);
    private final ConfigService configService = Mockito.mock(ConfigService.class);
    private final Liquibase liquibase = Mockito.mock(Liquibase.class);
    private final DatabaseInfoRepo databaseInfoRepo = Mockito.mock(DatabaseInfoRepo.class);
    private final TenantProducer tenantProducer = new TenantProducer();

    private final DatabaseService databaseService = new DatabaseService(databaseInfoRepo, tenantProducer);
    private final DatabaseResource databaseResource = new DatabaseResource(configService, databaseService);

    @BeforeEach
    void setUp() {
        databaseService.liquibaseFactoryBS = liquibaseFactory;
        databaseService.liquibaseFactoryWIN = liquibaseFactory;
        databaseService.liquibaseFactoryBE = liquibaseFactory;
        when(liquibaseFactory.createLiquibase()).thenReturn(liquibase);
    }

    @Test
    void resetDatabase_Success() throws Exception {
        // Arrange
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        doNothing().when(configService).validateNotProdAndDbResetFeatureEnabled();
        doNothing().when(liquibase).dropAll();

        // Act
        databaseResource.resetDatabase();

        // Assert
        verify(configService).validateNotProdAndDbResetFeatureEnabled();
        verify(liquibase).dropAll();
    }

    @Test
    void resetDatabase_ThrowsException_WhenLiquibaseFailsToDropAll() throws Exception {
        // Arrange
        doNothing().when(configService).validateNotProdAndDbResetFeatureEnabled();
        doThrow(new IllegalStateException("Some Database error")).when(liquibase).dropAll();

        // Act & Assert
        assertThrows(AppFailureException.class, databaseResource::resetDatabase);
    }
}
