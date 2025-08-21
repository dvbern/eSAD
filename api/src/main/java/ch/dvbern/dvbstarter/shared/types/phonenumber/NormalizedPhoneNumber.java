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

import java.io.Serial;
import java.io.Serializable;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Format;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage.invalidPhoneInSwissRegion;

@EqualsAndHashCode
@Schema(type = SchemaType.STRING, format = Format.PHONE_NUMBER, implementation = String.class)
public class NormalizedPhoneNumber implements Serializable {

    @Serial
    private static final long serialVersionUID = -3748429158254938767L;

    @NotNull
    @NonNull
    private final String phoneNumber;

    @Nullable
    public static NormalizedPhoneNumber parse(@Nullable String phoneNrString) {
        if (phoneNrString == null) {
            return null;
        }
        return new NormalizedPhoneNumber(phoneNrString);
    }

    public NormalizedPhoneNumber(@NonNull String input) {
        String normalized = StringUtils.normalizeSpace(input);
        if (!NormalizedPhoneNumberUtil.isValidPhoneNumber(normalized)) {
            throw new AppValidationException(invalidPhoneInSwissRegion(input));
        }
        this.phoneNumber = normalizePhoneNr(normalized);
    }

    /***
     * @return the phone number in International format with whitespace, e.g. +41 79 123 45 67
     */
    @NonNull
    public String getFormattedPhoneNumber() {
        return NormalizedPhoneNumberUtil.formatPhoneNumberHumanReadable(phoneNumber);
    }

    public boolean isMobileNumber() {
        return NormalizedPhoneNumberUtil.isMobileNumber(phoneNumber);
    }

    @Override
    public String toString() {
        return phoneNumber;
    }

    @NonNull
    private static String normalizePhoneNr(@NonNull String phoneNumberString) {
        // store like +41791234567
        return NormalizedPhoneNumberUtil.normalizePhoneNumber(phoneNumberString);
    }

}
