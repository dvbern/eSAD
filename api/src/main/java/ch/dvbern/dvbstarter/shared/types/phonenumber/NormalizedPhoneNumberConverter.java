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

package ch.dvbern.dvbstarter.shared.types.phonenumber;

import ch.dvbern.dvbstarter.shared.types.GenericStringConverter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Converter(autoApply = true)
public class NormalizedPhoneNumberConverter extends GenericStringConverter<NormalizedPhoneNumber> {

    public NormalizedPhoneNumberConverter() {
        super(NormalizedPhoneNumberConverter::toInternalForm, NormalizedPhoneNumberConverter::fromString);
    }

    @Nullable
    private static String toExternalForm(@Nullable NormalizedPhoneNumber phoneNr) {
        return phoneNr == null
            ? null
            : phoneNr.getFormattedPhoneNumber();
    }

    @Nullable
    private static String toInternalForm(@Nullable NormalizedPhoneNumber phoneNr) {
        return phoneNr == null
            ? null
            : phoneNr.toString();
    }

    @Nullable
    private static NormalizedPhoneNumber fromString(@Nullable String s) {
        return NormalizedPhoneNumber.parse(s);
    }

    public static void registerJackson(@NonNull SimpleModule module) {
        GenericStringConverter.registerJackson(
            module,
            NormalizedPhoneNumber.class,
            NormalizedPhoneNumberConverter::toExternalForm,
            NormalizedPhoneNumberConverter::fromString
        );
    }
}
