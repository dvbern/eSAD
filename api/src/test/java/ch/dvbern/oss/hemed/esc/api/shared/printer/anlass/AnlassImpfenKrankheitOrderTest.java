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

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AnlassImpfenKrankheitOrderTest {

    @Test
    void lists_every_Krankheit_exactly_once() {
        // Impfstoffe against a Krankheit missing here would not be listed in the specified order on the
        // "Ausdruck Impfen" print-out.
        assertThat(AnlassImpfenKrankheitOrder.KRANKHEITEN)
            .containsExactlyInAnyOrder(Krankheit.values());
    }

    @Test
    void lists_the_specified_order_with_Haemophilus_influenzae_last() {
        // The specified order of the print-out does not include Haemophilus influenzae, so it comes last.
        assertThat(AnlassImpfenKrankheitOrder.KRANKHEITEN)
            .containsExactly(
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
    }

    @Nested
    class ImpfstoffComparator {

        @Test
        void orders_Impfstoffe_by_their_first_Krankheit_in_the_specified_order() {
            // ARRANGE: in alphabetical order, which differs from the specified order and from the declaration
            // order of Krankheit (which declares Hepatitis B, HPV and FSME before Masern)
            List<Impfstoff> impfstoffe = List.of(
                impfstoff("Bexsero", Krankheit.MENINGOKOKKEN_B),
                impfstoff("Boostrix", Krankheit.DIPHTERIE, Krankheit.TETANUS, Krankheit.PERTUSSIS),
                impfstoff("FSME-Immun CC", Krankheit.FSME),
                impfstoff("Gardasil 9", Krankheit.HPV),
                impfstoff("Hbvaxpro", Krankheit.HEPATITIS_B),
                impfstoff("Hiberix", Krankheit.HAEMOPHILUS_INFLUENZAE),
                impfstoff("Menveo", Krankheit.MENINGOKOKKEN_A_C_W_Y),
                impfstoff("Poliorix", Krankheit.POLIO),
                impfstoff("Priorix", Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN),
                impfstoff("Varivax", Krankheit.VARIZELLEN)
            );

            // ACT
            List<Impfstoff> result = impfstoffe.stream()
                .sorted(AnlassImpfenKrankheitOrder.IMPFSTOFF_COMPARATOR)
                .toList();

            // ASSERT: Diphtherie, Poliomyelitis, Masern, Varizellen, Hepatitis B, FSME, HPV, Meningokokken B,
            // Meningokokken ACWY, Haemophilus influenzae
            assertThat(result)
                .extracting(Impfstoff::getName)
                .containsExactly(
                    "Boostrix",
                    "Poliorix",
                    "Priorix",
                    "Varivax",
                    "Hbvaxpro",
                    "FSME-Immun CC",
                    "Gardasil 9",
                    "Bexsero",
                    "Menveo",
                    "Hiberix"
                );
        }

        @Test
        void orders_Impfstoffe_with_the_same_first_Krankheit_by_name_ignoring_case() {
            // ARRANGE: all three start with Diphtherie; the lower-case name checks that the case is ignored
            List<Impfstoff> impfstoffe = List.of(
                impfstoff("Revaxis", Krankheit.DIPHTERIE, Krankheit.TETANUS, Krankheit.POLIO),
                impfstoff("dT-Impfstoff", Krankheit.DIPHTERIE, Krankheit.TETANUS),
                impfstoff("Boostrix", Krankheit.DIPHTERIE, Krankheit.TETANUS, Krankheit.PERTUSSIS)
            );

            // ACT
            List<Impfstoff> result = impfstoffe.stream()
                .sorted(AnlassImpfenKrankheitOrder.IMPFSTOFF_COMPARATOR)
                .toList();

            // ASSERT
            assertThat(result)
                .extracting(Impfstoff::getName)
                .containsExactly("Boostrix", "dT-Impfstoff", "Revaxis");
        }

        @Test
        void orders_an_Impfstoff_without_Krankheiten_last() {
            // ARRANGE: the setter of the Krankheiten rejects an empty list, so only inconsistent data gets here
            List<Impfstoff> impfstoffe = List.of(
                new Impfstoff("Alter Impfstoff", "", EntityAktivStatus.AKTIV),
                impfstoff("Hiberix", Krankheit.HAEMOPHILUS_INFLUENZAE)
            );

            // ACT
            List<Impfstoff> result = impfstoffe.stream()
                .sorted(AnlassImpfenKrankheitOrder.IMPFSTOFF_COMPARATOR)
                .toList();

            // ASSERT
            assertThat(result)
                .extracting(Impfstoff::getName)
                .containsExactly("Hiberix", "Alter Impfstoff");
        }
    }

    @NonNull
    private static Impfstoff impfstoff(@NonNull String name, @NonNull Krankheit... krankheiten) {
        return ImpfstoffBuilder.create(builder -> builder
            .withName(name)
            .forKrankheiten(krankheiten));
    }
}
