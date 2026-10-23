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

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AnlassImpfenUtilTest {

    @Mock
    private UntersuchungImpfungInternService untersuchungImpfungInternService;

    @Test
    void getGeplanteImpfstoffe_returns_nothing_without_Untersuchung() {
        // ARRANGE
        Einladung einladung = EinladungBuilder.create(builder -> builder.withUntersuchung(null));

        // ACT
        List<Impfstoff> result =
            AnlassImpfenUtil.getGeplanteImpfstoffe(einladung, untersuchungImpfungInternService);

        // ASSERT
        assertThat(result)
            .isEmpty();
    }

    @Test
    void getGeplanteImpfstoffe_returns_only_Impfstoffe_of_GEPLANT_Impfungen() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.create(builder -> {
        });
        Einladung einladung = EinladungBuilder.create(builder -> builder.withUntersuchung(untersuchung));

        Impfstoff priorix = ImpfstoffBuilder.create(impfstoffBuilder -> impfstoffBuilder
            .withName("Priorix")
            .forKrankheiten(Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN));
        UntersuchungImpfungIntern geplant = UntersuchungImpfungInternBuilder.create(builder -> builder
            .withImpfStatus(ImpfStatus.GEPLANT)
            .withImpfstoff(priorix));

        Impfstoff boostrix = ImpfstoffBuilder.create(impfstoffBuilder -> impfstoffBuilder
            .withName("Boostrix")
            .forKrankheiten(Krankheit.DIPHTERIE, Krankheit.TETANUS, Krankheit.PERTUSSIS));
        UntersuchungImpfungIntern durchgefuehrt = UntersuchungImpfungInternBuilder.create(builder -> builder
            .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
            .withImpfstoff(boostrix));

        when(untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId()))
            .thenReturn(List.of(geplant, durchgefuehrt));

        // ACT
        List<Impfstoff> result =
            AnlassImpfenUtil.getGeplanteImpfstoffe(einladung, untersuchungImpfungInternService);

        // ASSERT: only the Impfstoff of the GEPLANT Impfung is returned, not the one of the DURCHGEFUEHRT Impfung
        assertThat(result)
            .containsExactly(priorix);
    }

    @Test
    void getGeplanteImpfstoffe_returns_nothing_without_Impfungen() {
        // ARRANGE
        Untersuchung untersuchung = UntersuchungBuilder.create(builder -> {
        });
        Einladung einladung = EinladungBuilder.create(builder -> builder.withUntersuchung(untersuchung));

        when(untersuchungImpfungInternService.findByUntersuchung(untersuchung.getId())).thenReturn(List.of());

        // ACT
        List<Impfstoff> result =
            AnlassImpfenUtil.getGeplanteImpfstoffe(einladung, untersuchungImpfungInternService);

        // ASSERT
        assertThat(result)
            .isEmpty();
    }
}
