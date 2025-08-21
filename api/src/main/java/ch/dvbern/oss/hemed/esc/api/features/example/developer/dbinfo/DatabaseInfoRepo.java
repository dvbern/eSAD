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

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DatabaseInfoRepo {

    private static final String REVINFO_SEQ_NAME = "revinfo_seq";

    private final Db db;

    @NonNull
    public List<DBInfoConnectionSSLJson> getDBConnectionInfo() {

        String query = """
            SELECT
            datname,
                usename,
                ssl,
                client_addr
            FROM
                pg_stat_ssl
            INNER JOIN pg_stat_activity ON pg_stat_ssl.pid = pg_stat_activity.pid;;
            """;
        List<Object[]> result = db.getEntityManager().createNativeQuery(query).getResultList();

        List<DBInfoConnectionSSLJson> dbInfoConnectionSSLJsonList = new ArrayList<>();
        if (!result.isEmpty()) {
            for (Object[] row : result) {
                if (row.length == 4) {
                    DBInfoConnectionSSLJson dbInfoConnectionSSLJson = new DBInfoConnectionSSLJson(
                         row[0] !=null ? row[0].toString() : "" ,
                         row[1] !=null ? row[1].toString() : "" ,
                         row[2] !=null ? row[2].toString() : "" ,
                         row[3] !=null ? row[3].toString() : ""
                    );
                    dbInfoConnectionSSLJsonList.add(dbInfoConnectionSSLJson);
                } else {
                    throw new AppFailureException(AppFailureMessage.internalError(
                        "DB connection info query returned unexpected number of columns"));
                }
            }
        }
        return dbInfoConnectionSSLJsonList;
    }

    @NonNull
    public String getDBVersion() {
        return db.getEntityManager().createNativeQuery("SELECT version()")
            .getSingleResult()
            .toString();
    }

    public long getNextHibernateRevInfoSeqValue() {
        return (long) db.getEntityManager()
            .createNativeQuery("SELECT nextval('" + REVINFO_SEQ_NAME + "')")
            .getSingleResult();
    }

    public void setNextHibernateRevInfoSeqValue(long saveNextValueForHibernateRevInfoSeq) {
        db.getEntityManager().createNativeQuery("SELECT setval('" + REVINFO_SEQ_NAME + "', :nextValue)")
            .setParameter("nextValue", saveNextValueForHibernateRevInfoSeq)
            .getSingleResult();
    }
}
