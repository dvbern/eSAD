/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the fixed order of the Krankheiten by which the geplante Impfstoffe are listed on the
 * "Ausdruck Impfen" print-out.
 * Haemophilus influenzae is not part of the specified order and is therefore listed last.
 * Every {@link Krankheit} must be listed here, otherwise the Impfstoffe against it are not listed in the
 * specified order.
 */
@UtilityClass
public final class AnlassImpfenKrankheitOrder {

    public static final List<Krankheit> KRANKHEITEN = List.of(
        Krankheit.DIPHTERIE,
        Krankheit.TETANUS,
        Krankheit.PERTUSSIS,
        Krankheit.POLIO,
        Krankheit.MASERN,
        Krankheit.MUMPS,
        Krankheit.ROETELN,
        Krankheit.VARIZELLEN,
        Krankheit.HEPATITIS_B,
        Krankheit.FSME,
        Krankheit.HPV,
        Krankheit.MENINGOKOKKEN_B,
        Krankheit.MENINGOKOKKEN_A_C_W_Y,
        Krankheit.HAEMOPHILUS_INFLUENZAE
    );

    /**
     * Orders Impfstoffe by the first of their Krankheiten in {@link #KRANKHEITEN}, and Impfstoffe with the same
     * first Krankheit by name, ignoring case. An Impfstoff without any of these Krankheiten comes last.
     */
    public static final Comparator<Impfstoff> IMPFSTOFF_COMPARATOR = Comparator
        .comparingInt(AnlassImpfenKrankheitOrder::getPositionOfFirstKrankheit)
        .thenComparing(Impfstoff::getName, String.CASE_INSENSITIVE_ORDER);

    private static int getPositionOfFirstKrankheit(@NonNull Impfstoff impfstoff) {
        Set<Krankheit> krankheiten = impfstoff.getKrankheiten();
        return IntStream.range(0, KRANKHEITEN.size())
            .filter(position -> krankheiten.contains(KRANKHEITEN.get(position)))
            .findFirst()
            .orElse(KRANKHEITEN.size());
    }
}
