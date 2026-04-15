/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import lombok.Getter;

@Getter
public enum SchedulerTyp {

    BENUTZER_INAKVIEREN,
    DOCUMENT_QUEUE,
    KINDER_INAKTIVIEREN,
    DOCUMENT_QUEUE_CLEAN_UP,
    RELOAD_DUPLIKATE,
    DEACTIVATE_IMPFSTOFF_CHARGEN,
    DELETE_IMPFSTOFF_CHARGEN,
    ARCHIVE_DOC_DOWNLOAD_REMINDER,
    MARK_FOR_ARCHIVIERUNG,
    DOSSIER_QUEUE_PROCESSING,
    DOSSIER_QUEUE_CLEAN_UP,
    ARCHIVIERUNG_KIND_DELETE,
}
