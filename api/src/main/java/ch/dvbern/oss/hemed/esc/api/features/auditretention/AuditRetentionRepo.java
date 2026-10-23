/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.auditretention;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Zugriff auf die von Hibernate Envers verwalteten Historisierungstabellen ({@code *_aud}) und die
 * Revisionstabelle {@code revinfo}.
 * <p>
 * Die Tabellennamen werden bewusst dynamisch aus dem DB-Katalog ({@code information_schema}) ermittelt, damit
 * neu hinzukommende {@code @Audited}-Entities ohne Codeanpassung automatisch mitbereinigt werden.
 */
@Repository
@RequiredArgsConstructor
public class AuditRetentionRepo {

    static final String REVINFO_TABLE_NAME = "revinfo";
    static final String AUD_TABLE_SUFFIX = "_aud";
    static final String REVISION_COLUMN_NAME = "rev";

    private final Db db;

    /**
     * Ermittelt alle Historisierungstabellen des aktuellen Schemas (Mandanten-DB): Tabellen mit dem Suffix
     * {@code _aud}, welche eine Spalte {@code rev} besitzen.
     */
    @NonNull
    public List<String> findAudTableNames() {
        String query = """
            SELECT t.table_name
            FROM information_schema.tables t
            INNER JOIN information_schema.columns c
                ON c.table_schema = t.table_schema
                AND c.table_name = t.table_name
            WHERE t.table_schema = current_schema()
                AND t.table_type = 'BASE TABLE'
                AND t.table_name LIKE :audSuffixPattern ESCAPE '!'
                AND c.column_name = :revColumn
            ORDER BY t.table_name ASC
            """;

        List<?> result = db.getEntityManager().createNativeQuery(query)
            .setParameter("audSuffixPattern", "%!" + AUD_TABLE_SUFFIX)
            .setParameter("revColumn", REVISION_COLUMN_NAME)
            .getResultList();

        return result.stream()
            .map(String::valueOf)
            .toList();
    }

    /**
     * Liefert die ältesten Revisionen, welche vor dem Cutoff-Zeitpunkt erstellt wurden, aufsteigend sortiert und
     * auf die Batchgrösse begrenzt.
     */
    @NonNull
    public List<Long> findOldRevisionIds(long cutoffEpochMillis, int batchsize) {
        String query = """
            SELECT r.rev
            FROM revinfo r
            WHERE r.revtstmp < :cutoff
            ORDER BY r.revtstmp ASC, r.rev ASC
            LIMIT :batchsize
            """;

        List<?> result = db.getEntityManager().createNativeQuery(query)
            .setParameter("cutoff", cutoffEpochMillis)
            .setParameter("batchsize", batchsize)
            .getResultList();

        return result.stream()
            .map(rev -> ((Number) rev).longValue())
            .toList();
    }

    /**
     * Löscht alle Zeilen der übergebenen Historisierungstabelle, welche zur übergebenen Revision gehören.
     *
     * @param tableName muss aus {@link #findAudTableNames()} stammen (Whitelist aus dem DB-Katalog)
     * @return Anzahl gelöschter Zeilen
     */
    public int deleteAudRowsForRevision(@NonNull String tableName, @IgnoreForIdClassTest long revId) {
        assertValidAudTableName(tableName);

        String query = "DELETE FROM " + tableName + " WHERE rev = :rev";

        return db.getEntityManager().createNativeQuery(query)
            .setParameter("rev", revId)
            .executeUpdate();
    }

    /**
     * Löscht die Revision selbst. Darf erst aufgerufen werden, wenn alle {@code _aud}-Zeilen dieser Revision
     * bereits gelöscht wurden (Fremdschlüssel auf {@code revinfo.rev}).
     *
     * @return Anzahl gelöschter Zeilen
     */
    public int deleteRevisionInfo(@IgnoreForIdClassTest long revId) {
        String query = "DELETE FROM " + REVINFO_TABLE_NAME + " WHERE rev = :rev";

        return db.getEntityManager().createNativeQuery(query)
            .setParameter("rev", revId)
            .executeUpdate();
    }

    /**
     * Schutz gegen SQL-Injection: Tabellennamen werden zwar nur aus dem DB-Katalog gelesen, trotzdem wird der
     * Name vor der Verwendung im dynamischen SQL validiert.
     */
    private static void assertValidAudTableName(@NonNull String tableName) {
        boolean valid = tableName.endsWith(AUD_TABLE_SUFFIX)
            && tableName.matches("[a-z0-9_]+");
        if (!valid) {
            throw new AppFailureException(AppFailureMessage.internalError(
                "Ungueltiger Name einer Envers-Historisierungstabelle: " + tableName));
        }
    }
}
