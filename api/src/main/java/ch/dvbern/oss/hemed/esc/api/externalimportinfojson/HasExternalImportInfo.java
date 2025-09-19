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

package ch.dvbern.oss.hemed.esc.api.externalimportinfojson;

import jakarta.persistence.Transient;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface HasExternalImportInfo {

    @Nullable
    ExternalImportInfo getExternalImportInfo();

    @Transient
    @Nullable
    default String getExternalSystemIdOrNull() {
        if (getExternalImportInfo() == null) {
            return null;
        }
        return getExternalImportInfo().getIdInExternalSystem();
    }

    @Transient
    default boolean isManualOrAktivInExternalSystem() {
        if (getExternalSystemIdOrNull() == null) {
            return true; // manual object therefore true
        }
        return isAktivInExternalSystem();
    }

    @Transient
    default boolean isAktivInExternalSystem() {
        var externalImportInfo = getExternalImportInfo();
        // here we should be talking about an imported object. Check if it is active in the external system
        return externalImportInfo != null && Boolean.TRUE.equals(externalImportInfo.getAktivInExternalSystem());
    }
}
