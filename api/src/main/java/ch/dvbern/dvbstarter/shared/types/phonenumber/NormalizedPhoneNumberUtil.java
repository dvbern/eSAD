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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@UtilityClass
public final class NormalizedPhoneNumberUtil {

    public static final String SWISS_CODE = "CH";

    private static final PhoneNumberUtil PHONE_NR_UTIL = PhoneNumberUtil.getInstance();

    /**
     * Benutzt google phonelib um zu ueberpruefen ob die nummer eine Mobile nummer ist.
     * ACHTUNG: Nummern vom typ FIXED_LINE_OR_MOBILE werden als false evaluirt.
     *
     * @param phoneNumber die zu ueberpruefende nummer
     * @return ob die nummer vom typ PhoneNumberTyp.MOBILE ist.
     */
    static boolean isMobileNumber(@Nullable String phoneNumber) {
        try {
            Phonenumber.PhoneNumber phone = PHONE_NR_UTIL.parse(phoneNumber, SWISS_CODE);
            return PHONE_NR_UTIL.isValidNumber(phone) && PHONE_NR_UTIL.getNumberType(phone) == PhoneNumberType.MOBILE;
        } catch (NumberParseException e) {
            return false;
        }
    }

    static boolean isValidPhoneNumber(@NonNull String phoneNumber) {
        return isValidPhoneNumber(phoneNumber, SWISS_CODE); // default country code if not provided in phoneNr is CH
    }

    static boolean isValidPhoneNumber(String phoneNumber, String countryCode) {
        try {
            PhoneNumber number = PHONE_NR_UTIL.parse(phoneNumber, countryCode);
            return PHONE_NR_UTIL.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }

    /**
     * normalize to e164, This does NOT validate the number
     */
    @NonNull
    static String normalizePhoneNumber(@NonNull String phoneNumber) {
        phoneNumber = normalizePhoneNumberUsingFormat(phoneNumber, PhoneNumberFormat.E164);
        return phoneNumber;
    }

    @NonNull
    static String formatPhoneNumberHumanReadable(@NonNull String phoneNr) {
        return NormalizedPhoneNumberUtil.normalizePhoneNumberUsingFormat(phoneNr, PhoneNumberFormat.INTERNATIONAL);
    }

    @NonNull
    static String normalizePhoneNumberUsingFormat(
        @NonNull String input,
        @NonNull PhoneNumberFormat phoneNumberFormat
    ) {
        String result = input;
        try {
            Phonenumber.PhoneNumber phone = PHONE_NR_UTIL.parse(input, SWISS_CODE);
            result = PHONE_NR_UTIL.format(phone, phoneNumberFormat);
        } catch (NumberParseException e) {
            LOG.debug("Invalid phone number {}, number will be used unchanged", input, e);
        }
        return result;
    }
}
