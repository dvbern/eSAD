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

package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleValueEnhancedMapper {

    @NonNull
    public static <E extends Enum<E>> ValueEnhancedJson toJson(
        @NonNull Class<E> clazz,
        @Nullable SingleValueEnhanced<E> entity
    ) {
        if (entity == null) {
            return new ValueEnhancedJson(null, null);
        }
        String enumValueName = entity.toEnumValueName(clazz);
        return new ValueEnhancedJson(
            enumValueName != null ? List.of(enumValueName) : null,
            entity.getNichtGeprueftGrund()
        );
    }

    @NonNull
    public static <E extends Enum<E>> SingleValueEnhanced<E> toEntity(
        @NonNull Class<E> clazz,
        @Nullable ValueEnhancedJson saveJson
    ) {
        if (saveJson == null) {
            return SingleValueEnhanced.of();
        }
        List<String> values = saveJson.getValue();
        if (values == null || values.isEmpty()) {
            return SingleValueEnhanced.of();
        }
        String first = values.getFirst();
        if (StringUtils.isEmpty(first)) {
            return SingleValueEnhanced.of();
        }
        return SingleValueEnhanced.of(
            Enum.valueOf(clazz, first),
            saveJson.getNichtGeprueftGrund()
        );
    }
}
