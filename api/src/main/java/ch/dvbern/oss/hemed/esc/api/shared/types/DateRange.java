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

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckDateRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

@Embeddable
@CheckDateRange
@Getter
@Setter
public class DateRange implements Serializable, Comparable<DateRange> {

    @Serial
    private static final long serialVersionUID = 8244737446639845584L;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate datumVon;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate datumBis;

    protected DateRange(@NonNull LocalDate datumVon, @NonNull LocalDate datumBis) {
        this.datumVon = checkNotNull(datumVon);
        this.datumBis = checkNotNull(datumBis);
    }

    public DateRange() {
        this(DateConst.BEGIN_OF_TIME, DateConst.END_OF_TIME);
    }

    protected DateRange(@NonNull LocalDate stichtag) {
        this(stichtag, stichtag);
    }

    protected DateRange(@NonNull DateRange gueltigkeit) {
        this(gueltigkeit.datumVon, gueltigkeit.datumBis);
    }

    /**
     * von <= bis
     */
    public boolean hasValidRange() {
        return !datumVon.isAfter(datumBis);
    }

    @NonNull
    public static SortedSet<DateRange> findZeitraeume(@NonNull SortedSet<LocalDate> stichtage) {
        SortedSet<DateRange> zeitraeume = new TreeSet<>();
        LocalDate von = stichtage.first();
        stichtage.remove(stichtage.first());
        for (LocalDate stichtag : stichtage) {
            // der Stichtag ist der Tag an dem die Aenderung aktiv wird => der RZeitraum endet einen Tag davor;
            DateRange zeitraum = DateRange.of(von, stichtag.minusDays(1));
            zeitraeume.add(zeitraum);
            von = stichtag;
        }
        return zeitraeume;
    }

    @NonNull
    public static DateRange of(@NonNull LocalDate gueltigAb, @NonNull LocalDate gueltigBis) {
        return new DateRange(gueltigAb, gueltigBis);
    }

    /**
     * stichtag == gueltigAb == gueltigBis
     */
    @NonNull
    public static DateRange of(@NonNull LocalDate stichtag) {
        return new DateRange(stichtag);
    }

    /**
     * Creates a copy.
     */
    @NonNull
    public static DateRange of(@NonNull DateRange gueltigkeit) {
        return new DateRange(gueltigkeit);
    }

    @NonNull
    public static DateRange ofMonthContaining(@NonNull LocalDate date) {
        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfMonth());
        return DateRange.of(firstDay, lastDay);
    }

    @NonNull
    public static DateRange ofYearContaining(@NonNull LocalDate date) {
        LocalDate firstDay = date.with(TemporalAdjusters.firstDayOfYear());
        LocalDate lastDay = date.with(TemporalAdjusters.lastDayOfYear());
        return DateRange.of(firstDay, lastDay);
    }

    @NonNull
    public static DateRange ofFullRange() {
        return new DateRange(DateConst.BEGIN_OF_TIME, DateConst.END_OF_TIME);
    }

    /**
     * true, when the other DateRange is completely contained in this DateRange
     */
    public boolean contains(@NonNull DateRange other) {
        return !(datumVon.isAfter(other.datumVon) || datumBis.isBefore(other.datumBis));
    }

    /**
     * gueltigAb <= date <= gueltigBis
     */
    public boolean contains(@NonNull LocalDate date) {
        return DateUtil.contains(date, datumVon, datumBis);
    }

    /**
     * gueltigAb < date && gueltigBis < date
     */
    public boolean isBefore(@NonNull LocalDate date) {
        return datumVon.isBefore(date) && datumBis.isBefore(date);
    }

    /**
     * gueltigAb > date && gueltigBis > date
     */
    public boolean isAfter(@NonNull LocalDate date) {
        return datumVon.isAfter(date) && datumBis.isAfter(date);
    }

    /**
     * gueltigAb == date + 1 Day
     */
    public boolean startsDayAfter(@NonNull LocalDate date) {
        return datumVon.equals(date.plusDays(1));
    }

    /**
     * gueltigBis == date - 1 Day
     */
    public boolean endsDayBefore(@NonNull LocalDate date) {
        return datumBis.equals(date.minusDays(1));
    }

    /**
     * gueltigBis == gueltigAb
     */
    @XmlTransient
    @JsonIgnore
    public boolean isStichtag() {
        return datumVon.equals(datumBis);
    }

    /**
     * gueltigAb == other.gueltigBis + 1 Day
     */
    public boolean startsDayAfter(@NonNull DateRange other) {
        return startsDayAfter(other.datumBis);
    }

    /**
     * gueltigBis == other.gueltigAb - 1 Day
     */
    public boolean endsDayBefore(@NonNull DateRange other) {
        return endsDayBefore(other.datumVon);
    }

    /**
     * Neue DateRange, mit gueltigAb auf den vorherigen Montag und gueltigBis auf den naechsten Sonntag setzt.
     * Use-Case z.B.: einen Stichtag auf die ganze Woche ausdehnen.
     */
    @NonNull
    public DateRange withFullWeeks() {
        LocalDate montag = datumVon.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sonntag = datumBis.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return DateRange.of(montag, sonntag);
    }

    /**
     * Neue DateRange, mit gueltigAb auf den ersten Tag des Monats von gueltigAb
     * un dem lezten Tag des Monats von gueltigBs.
     * Kann also mehrere Monate umspannen!
     */
    @NonNull
    public DateRange withFullMonths() {
        LocalDate firstDay = datumVon.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = datumBis.with(TemporalAdjusters.lastDayOfMonth());
        return DateRange.of(firstDay, lastDay);
    }

    /**
     * Neue DateRange, mit gueltigAb auf den ersten Tag des Jahres von gueltigAb
     * und gueltigBis auf den letzten Tag des Jahres von gueltigBis.
     * Kann also mehrere Jahre umspannen!
     */
    @NonNull
    public DateRange withFullYears() {
        LocalDate firstDay = datumVon.with(TemporalAdjusters.firstDayOfYear());
        LocalDate lastDay = datumBis.with(TemporalAdjusters.lastDayOfYear());
        return DateRange.of(firstDay, lastDay);
    }

    /**
     * @return Falls es zwischen dieser DateRange und otherRange eine zeitliche ueberlappung gibt, so wird diese
     * zurueck gegeben
     */
    @NonNull
    public Optional<DateRange> getOverlap(@NonNull DateRange otherRange) {
        if (datumVon.isAfter(otherRange.datumBis) ||
            datumBis.isBefore(otherRange.datumVon)) {
            return Optional.empty();
        }
        LocalDate ab = otherRange.datumVon.isAfter(datumVon) ?
            otherRange.datumVon :
            datumVon;
        LocalDate bis = otherRange.datumBis.isBefore(datumBis) ?
            otherRange.datumBis :
            datumBis;
        return Optional.of(DateRange.of(ab, bis));
    }

    /**
     * {@link #getOverlap(DateRange)}.isPresent()
     */
    public boolean intersects(@NonNull DateRange other) {
        return getOverlap(other).isPresent();
    }

    /**
     * @return Falls es zwischen dieser DateRange und otherRange eine zeitliche Lücke gibt, so wird diese
     * zurueck gegeben
     */
    @NonNull
    public Optional<DateRange> getGap(@NonNull DateRange otherRange) {
        DateRange earlierRange = this.datumVon.isBefore(otherRange.datumVon) ? this : otherRange;
        DateRange laterRange = this.datumVon.isAfter(otherRange.datumVon) ? this : otherRange;
        long gap = earlierRange.datumBis.until(laterRange.datumVon, ChronoUnit.DAYS);
        if (gap > 1) {
            return Optional.of(DateRange.of(
                earlierRange.datumBis.plusDays(1),
                laterRange.datumVon.minusDays(1)
            ));
        }
        return Optional.empty();
    }

    /**
     * {@link #getOverlap(DateRange)}.isPresent()
     */
    public boolean hasGap(@NonNull DateRange other) {
        return getGap(other).isPresent();
    }

    /**
     * @return Zeitraeume dieser DateRange, welche den Range von {@code other} nicht beinhalten. Falls es keine
     * Ueberlappung gibt, wird eine leere Liste zurueck gegeben.
     *
     * <p>Beispiel</p>
     * <table summary="Beispiel">
     * <tr>
     * <th>Objects</th>
     * <th>Zeitstrahlen</th>
     * </tr>
     * <tr>
     * <td>this</td>
     * <td><pre>|--------|</pre></td>
     * </tr>
     * <tr>
     * <td>other</td>
     * <td><pre>   |---|  </pre></td>
     * </tr>
     * <tr>
     * <td>results</td>
     * <td><pre>|--|   |-|</pre></td>
     * </tr>
     * </table>
     */
    @NonNull
    public List<DateRange> except(@NonNull DateRange other) {
        if (!intersects(other)) {
            return Collections.emptyList();
        }
        SortedSet<LocalDate> stichtage = Stream.concat(streamStichtage(), other.streamStichtage())
            .collect(Collectors.toCollection(TreeSet::new));
        return findZeitraeume(stichtage).stream()
            .filter(r -> !r.intersects(other))
            .toList();
    }

    /**
     * @return counts the number of days between gueltigAb and gueltigBis (inclusive gueltigAb and gueltigBis)
     */
    @XmlTransient
    @JsonIgnore
    public long getDays() {
        return ChronoUnit.DAYS.between(datumVon, datumBis) + 1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DateRange other)) {
            return false;
        }
        return 0 == compareTo(other);
    }

    @Override
    public int hashCode() {
        int result = getDatumVon().hashCode();
        result = 31 * result + getDatumBis().hashCode();
        return result;
    }

    /**
     * Natural ordering: zuerst gueltigAb vergleichen, dann gueltigBis
     */
    @Override
    public int compareTo(@NonNull DateRange o) {
        checkNotNull(o);
        int cmp = getDatumVon().compareTo(o.getDatumVon());
        if (cmp == 0) {
            cmp = getDatumBis().compareTo(o.getDatumBis());
        }
        return cmp;
    }

    /**
     * Convenience: {@link Stream#of(Object)} {@link #datumVon}, {@link #datumBis}
     */
    @NonNull
    public Stream<LocalDate> stream() {
        return Stream.of(datumVon, datumBis);
    }

    /**
     * Ein Stichtag ist der Tag, ab dem eine Aenderung aktiv wird.
     * Der erste Stichtag einer DateRange ist also gueltigAb und der Zweite Stichtag ist der Tag <b>nach</b> gueltigBis
     */
    @NonNull
    public Stream<LocalDate> streamStichtage() {
        return Stream.of(datumVon, datumBis.plusDays(1));
    }

    /**
     * @return a stream of all days between {@link DateRange#datumVon} and {@link DateRange#datumBis} (inclusive).
     */
    @NonNull
    public Stream<LocalDate> streamDays() {
        return Stream.iterate(0, i -> i + 1)
            .limit(ChronoUnit.DAYS.between(datumVon, datumBis) + 1)
            .map(i -> datumVon.plusDays(i));
    }

    @NonNull
    public String format(@NonNull DateTimeFormatter formatter, @NonNull String joiner) {
        if (isStichtag()) {
            return datumVon.format(formatter);
        }
        return datumVon.format(formatter) + joiner + datumBis.format(formatter);
    }

    @NonNull
    public String format(@NonNull DateTimeFormatter formatter) {
        return format(formatter, " - ");
    }

    @Override
    public String toString() {
        return  format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN), " - ");
    }
}
