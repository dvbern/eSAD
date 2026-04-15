/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.liquibase.LiquibaseDataSource;
import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.enterprise.inject.Instance;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.util.List;
import java.util.Optional;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.ConfigProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {

    @LiquibaseDataSource("bs")
    public Instance<LiquibaseFactory>  liquibaseFactoryBS;

    @LiquibaseDataSource("win")
    public Instance<LiquibaseFactory>  liquibaseFactoryWIN;

    @LiquibaseDataSource("be")
    public Instance<LiquibaseFactory> liquibaseFactoryBE;

    private final DatabaseInfoRepo databaseInfoRepo;

    private final TenantProducer tenantProducer;

    /**
     * Resets the database and reinitializes it while preserving the current value of the revinfo_seq
     * This is important because otherwise the sequence value would be reset to 1 but existing connections
     * might still have higher values because they are reserved/cached in incremetns of 50
     * This would lead to a conflict once the sequence re-reaches the value of the cached value
     */
    @Transactional(TxType.NOT_SUPPORTED)
    public void resetAndReinitDatabasePreservingHibernateSequenceValue(@NonNull TenantEnum tenant) {
        LOG.info("DB-RESET: Resetting and reinitializing database for tenant: {}", tenant);
        LiquibaseFactory liquibaseFactory = createLiquibaseFactory(tenant);
        try (Liquibase ourSingleLiquibase = liquibaseFactory.createLiquibase()) {

            tenantProducer.setCurrentTenant(tenant);
            long nextHibernateRevInfoSeqValue = databaseInfoRepo.getNextHibernateRevInfoSeqValue();
            ourSingleLiquibase.dropAll();
            liquibaseUpdate(tenant, ourSingleLiquibase);
            // sequence is incremented by 50 so we skip a block of 50 values
            long saveNextValueForHibernateRevInfoSeq = nextHibernateRevInfoSeqValue + 50;
            databaseInfoRepo.setNextHibernateRevInfoSeqValue(saveNextValueForHibernateRevInfoSeq);
            LOG.info("DB-RESET: FINISHED Resetting and reinitializing database for tenant: {}", tenant);
        } catch (Exception e) {
            throw new AppFailureException(AppFailureMessage.internalError("Could not init database"), e);
        }

    }

    private void liquibaseUpdate(@NonNull TenantEnum tenant, @NonNull Liquibase liquibase) throws LiquibaseException {
        String hibernateMultiTenancyIdentifier = tenant.getHibernateMultiTenancyIdentifier();
        Optional<String> context = ConfigProvider.getConfig()
            .getOptionalValue("quarkus.liquibase." + hibernateMultiTenancyIdentifier + ".contexts", String.class);
        if (context.isPresent()) {
            liquibase.update(context.get());
        } else {
            liquibase.update();
        }
    }

    @NonNull
    public List<DBInfoConnectionSSLJson> getDBInfo() {
        return databaseInfoRepo.getDBConnectionInfo();
    }

    @NonNull
    public String getDBVersion() {
        return databaseInfoRepo.getDBVersion();
    }

    @NonNull
    private LiquibaseFactory createLiquibaseFactory(@NonNull TenantEnum tenant) {
        tenantProducer.setCurrentTenant(tenant);
        if (TenantEnum.BS == tenant) {
            return liquibaseFactoryBS.get();
        }
        if (TenantEnum.WIN == tenant) {
            return liquibaseFactoryWIN.get();
        }
        if (TenantEnum.BE == tenant) {
            return liquibaseFactoryBE.get();
        }
        throw new AppFailureException(AppFailureMessage.internalError("Unknown tenant: " + tenant));

    }
}
