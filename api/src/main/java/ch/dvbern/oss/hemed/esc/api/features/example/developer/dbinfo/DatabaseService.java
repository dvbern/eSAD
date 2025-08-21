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

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import liquibase.Liquibase;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.ConfigProvider;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final LiquibaseFactory liquibaseFactory;

    private final DatabaseInfoRepo databaseInfoRepo;

    /**
     * Resets the database and reinitializes it while preserving the current value of the revinfo_seq
     * This is important because otherwise the sequence value would be reset to 1 but existing connections
     * might still have higher values because they are reserved/cached in incremetns of 50
     * This would lead to a conflict once the sequence re-reaches the value of the cached value
     */
    @Transactional(TxType.NOT_SUPPORTED)
    public void resetAndReinitDatabasePreservingHibernateSequenceValue() {
        long nextHibernateRevInfoSeqValue = databaseInfoRepo.getNextHibernateRevInfoSeqValue();
        resetDatabase();
        liquibaseUpdate();
        // sequence is incremented by 50 so we skip a block of 50 values
        long saveNextValueForHibernateRevInfoSeq = nextHibernateRevInfoSeqValue + 50;
        databaseInfoRepo.setNextHibernateRevInfoSeqValue(saveNextValueForHibernateRevInfoSeq);
    }

    private void resetDatabase() {
        try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
            liquibase.dropAll();
        } catch (Exception e) {
            throw new AppFailureException(AppFailureMessage.internalError("Could not drop database"), e);
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public void initDatabase() {
        liquibaseUpdate();
    }

    private void liquibaseUpdate() {
        Optional<String> context = ConfigProvider.getConfig().getOptionalValue("quarkus.liquibase.contexts", String.class);
        try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
            if (context.isPresent()) {
                liquibase.update(context.get());
            } else {
                liquibase.update();
            }
        } catch (Exception e) {
            throw new AppFailureException(AppFailureMessage.internalError("Could not init database"), e);
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
}
