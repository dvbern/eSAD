/*
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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.UntersuchungTabsSavedJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EinladungMapperTest {

    @ParameterizedTest(name = "toUntersuchungTabsSavedJson should return [{3}] when UntersuchungImpfung {4}")
    @MethodSource("getUntersuchungen")
    void toUntersuchungTabsSavedJson_should_return_untersuchungImpfungen_saved(
        Untersuchung untersuchung,
        List<UntersuchungImpfstatusKrankheit> impfstatus,
        List<UntersuchungImpfungIntern> impfungenInternList,
        boolean expected,
        String testCase) {
        // ACT
        UntersuchungTabsSavedJson actual =
            EinladungMapper.toUntersuchungTabsSavedJson(untersuchung, impfstatus, impfungenInternList);

        // ASSERT
        Assertions.assertThat(actual.isImpfungenTabSaved()).isEqualTo(expected);
    }

    static Stream<Arguments> getUntersuchungen() {
        return Stream.of(
            Arguments.of(UntersuchungBuilder.defaultUntersuchung(LocalDate.now()), List.of(), List.of(), false, "UntersuchungImpfung is null"),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.defaultUntersuchungImpfungen(
                    LocalDate.now()))), List.of(), List.of(), false, "UntersuchungImpfung is empty"),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.defaultUntersuchungImpfungen(
                    LocalDate.now()))),
                List.of(UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        UntersuchungImpfungenBuilder.defaultUntersuchungImpfungen(LocalDate.now()))
                    .forKrankheit(Krankheit.HEPATITIS_B))),
                List.of(),
                true,
                "UntersuchungImpfung has impfstatus list not empty "),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.defaultUntersuchungImpfungen(
                    LocalDate.now()))),
                List.of(),
                List.of(UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now())),
                true,
                "UntersuchungImpfung has impfungenIntern list not empty "),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.create(
                    untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder.withVisum1(
                        BenutzerBuilder.defaultBenutzer(),
                        LocalDate.now())))), List.of(), List.of(), false, "with visum 1 is ignored"),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.create(
                    untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder.withVisum2(
                        BenutzerBuilder.defaultBenutzer(),
                        LocalDate.now())))), List.of(),List.of(), false, "with visum 2 is ignored"),
            Arguments.of(
                UntersuchungBuilder.create(builder -> builder.withUntersuchungImpfungen(UntersuchungImpfungenBuilder.create(
                    untersuchungImpfungenBuilder -> untersuchungImpfungenBuilder.withUntersuchungImpfungenMassnahmen(
                        UntersuchungImpfungenMassnahmenBuilder.defaultUntersuchungImpfungenMassnahmen())))),
                List.of(),
                List.of(),
                false,
                "UntersuchungImpfungenMassnahmen is ignored"));
    }
}
