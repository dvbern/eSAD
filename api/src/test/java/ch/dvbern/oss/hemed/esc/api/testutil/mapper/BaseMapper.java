package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
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
    default String map(@Nullable EdokidId edokidId) {
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
