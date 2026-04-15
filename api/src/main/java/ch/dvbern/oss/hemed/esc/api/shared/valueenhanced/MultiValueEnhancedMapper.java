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
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiValueEnhancedMapper {

    @NonNull
    public static <E extends Enum<E>> ValueEnhancedJson toJson(
        @NonNull Class<E> clazz,
        @Nullable MultiValueEnhanced<E> entity
    ) {
        if (entity == null) {
            return new ValueEnhancedJson(null, null);
        }
        List<E> list = EnumSetUtil.toList(entity.getEnumValues(clazz));
        List<String> valuesAsString = list.stream().map(Enum::name).toList();
        return new ValueEnhancedJson(
            valuesAsString,
            entity.getNichtGeprueftGrund()
        );
    }

    @NonNull
    public static <E extends Enum<E>> MultiValueEnhanced<E> toEntity(
        @NonNull Class<E> clazz,
        @Nullable ValueEnhancedJson saveJson
    ) {
        if (saveJson == null) {
            return MultiValueEnhanced.of();
        }
        List<E> enumValues = saveJson.getValue() == null ? List.of() : saveJson.getValue().stream()
            .map(s -> Enum.valueOf(clazz, s))
            .toList();
        return MultiValueEnhanced.of(
            EnumSetUtil.toEnumSet(enumValues, clazz),
            saveJson.getNichtGeprueftGrund()
        );
    }
}
