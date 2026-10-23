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

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Verarbeitet eine einzelne Envers-Revision als eigene, in sich abgeschlossene Transaktion, damit ein Fehler bei
 * einer Revision die nachfolgenden Revisionen nicht blockiert.
 */
@Service
@RequiredArgsConstructor
public class AuditRetentionCleanupHelperService {

    private final AuditRetentionRepo auditRetentionRepo;

    /**
     * Löscht alle Historisierungszeilen der übergebenen Revision in allen übergebenen {@code _aud}-Tabellen und
     * erst danach die Revision selbst.
     *
     * @return Anzahl gelöschter Zeilen (inklusive der {@code revinfo}-Zeile)
     */
    @Transactional(TxType.REQUIRES_NEW)
    public int cleanupRevision(@NonNull List<String> audTableNames, @IgnoreForIdClassTest long revId) {
        int deletedRows = 0;
        for (String audTableName : audTableNames) {
            deletedRows += auditRetentionRepo.deleteAudRowsForRevision(audTableName, revId);
        }
        deletedRows += auditRetentionRepo.deleteRevisionInfo(revId);

        return deletedRows;
    }
}
