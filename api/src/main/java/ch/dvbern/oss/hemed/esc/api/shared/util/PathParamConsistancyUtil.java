/*
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

package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PathParamConsistancyUtil {

	private PathParamConsistancyUtil() {
	}

	public static void requireForeignIdsAreEqual(
        @NonNull ID<?> entityId,
        @Nullable ID<?> actualForeigndId,
        @NonNull ID<?> expectedForeignId
    ) {
		if (!expectedForeignId.equals(actualForeigndId)) {
			throw new AppValidationException(AppValidationMessage.invalidEntityRelation(entityId, expectedForeignId, actualForeigndId));
		}
	}

	public static void requireContainsForeignId(
        @NonNull ID<?> entityId,
        @NonNull List<? extends ID<?>> actualForeigndIds,
        @NonNull ID<?>  expectedForeignId
    ) {
		if (actualForeigndIds.stream().noneMatch(expectedForeignId::equals)) {
			throw new AppValidationException(AppValidationMessage.invalidEntityRelation(entityId, expectedForeignId,  actualForeigndIds));
		}
	}
}
