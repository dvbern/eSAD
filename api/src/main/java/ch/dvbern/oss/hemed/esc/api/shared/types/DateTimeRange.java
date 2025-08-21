/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.types;

import java.io.Serializable;
import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckDateTimeRange;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Setter
@Getter
@EqualsAndHashCode
@Embeddable
@CheckDateTimeRange
public class DateTimeRange implements Serializable {

    private static final long serialVersionUID = -2321415284318466343L;

    @NonNull
    @NotNull
    private LocalDateTime von;

    @NonNull
    @NotNull
    private LocalDateTime bis;

    /**
     * Only makes sense when used by JPA!.
     */
    protected DateTimeRange() {
        von = DateConst.BEGIN_OF_DATETIME;
        bis = DateConst.END_OF_DATETIME;
    }

    public DateTimeRange(@NonNull LocalDateTime von, @NonNull LocalDateTime bis) {
        this.von = von;
        this.bis = bis;
    }

    @NonNull
    public static DateTimeRange of(@NonNull LocalDateTime von, @NonNull LocalDateTime bis) {
        return new DateTimeRange(von, bis);
    }

    @NonNull
    public static DateTimeRange of(@NonNull DateTimeRange other) {
        return new DateTimeRange(other.getVon(), other.getBis());
    }

    /**
     * von <= time <= bis
     */
    public boolean isInRange(@NonNull LocalDateTime time) {
        return !(time.isBefore(getVon()) || time.isAfter(getBis()));
    }

    public boolean hasValidRange() {
        return !getVon().isAfter(getBis());
    }
}
