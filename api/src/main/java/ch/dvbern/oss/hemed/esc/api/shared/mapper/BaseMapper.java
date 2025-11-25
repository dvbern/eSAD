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

package ch.dvbern.oss.hemed.esc.api.shared.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base mapper for mapping types to strings. All mappers should extend this interface.
 */
public interface BaseMapper {
    @NonNull
    default String map(@NonNull ID<?> id) {
        return id.getId();
    }

    @Nullable
    default String map(@Nullable EmailAddress email) {
        return email != null ? email.toString() : null;
    }

    @Nullable
    default String map(@Nullable NormalizedPhoneNumber phone) {
        return phone != null ? phone.toString() : null;
    }

    @Nullable
    default String map(@Nullable SozialversicherungsNummer svn) {
        return svn == null ? null : svn.toString();
    }

    @Nullable
    default String map(@IgnoreForIdClassTest @Nullable EdokidId edokidId) {
        return edokidId == null ? null : edokidId.toString();
    }

    @Nullable
    default LocalDateTime map(@Nullable OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    @Nullable
    default OffsetDateTime map(@Nullable LocalDateTime value) {
        return value != null ? value.atOffset(OffsetDateTime.now().getOffset()) : null;
    }
}
