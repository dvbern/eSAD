package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseInfoRepo;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.enterprise.inject.Instance;
import liquibase.Liquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseResourceTest {

    private final LiquibaseFactory liquibaseFactory = Mockito.mock(LiquibaseFactory.class);
    private final Instance<LiquibaseFactory> liquibaseFactoryInstance = Mockito.mock(Instance.class);
    private final ConfigService configService = Mockito.mock(ConfigService.class);
    private final Liquibase liquibase = Mockito.mock(Liquibase.class);
    private final DatabaseInfoRepo databaseInfoRepo = Mockito.mock(DatabaseInfoRepo.class);
    private final PerTenantRunnerService perTenantRunnerService = Mockito.mock(PerTenantRunnerService.class);

    private final DatabaseService databaseService = new DatabaseService(databaseInfoRepo, perTenantRunnerService);
    private final DatabaseResource databaseResource = new DatabaseResource(configService, databaseService);

    @BeforeEach
    void setUp() {
        Mockito.when(liquibaseFactoryInstance.get()).thenReturn(liquibaseFactory);
        databaseService.liquibaseFactoryBS = liquibaseFactoryInstance;
        databaseService.liquibaseFactoryWIN = liquibaseFactoryInstance;
        databaseService.liquibaseFactoryBE = liquibaseFactoryInstance;
        when(liquibaseFactory.createLiquibase()).thenReturn(liquibase);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(1).run();
            return null;
        }).when(perTenantRunnerService).runForTenant(any(TenantEnum.class), any(Runnable.class));
    }

    @Test
    void resetDatabase_Success() throws Exception {
        // ARRANGE
        doNothing().when(configService).validateNotProdAndDbResetFeatureEnabled();
        doNothing().when(liquibase).dropAll();

        // ACT
        databaseResource.resetDatabase();

        // ASSERT
        verify(configService).validateNotProdAndDbResetFeatureEnabled();
        verify(perTenantRunnerService).runForTenant(eq(TenantEnum.BS), any(Runnable.class));
        verify(liquibase).dropAll();
    }

    @Test
    void resetDatabase_ThrowsException_WhenLiquibaseFailsToDropAll() throws Exception {
        // ARRANGE
        doNothing().when(configService).validateNotProdAndDbResetFeatureEnabled();
        doThrow(new IllegalStateException("Some Database error")).when(liquibase).dropAll();

        // ACT & ASSERT
        assertThrows(AppFailureException.class, databaseResource::resetDatabase);
    }
}
