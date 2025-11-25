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

package ch.dvbern.oss.hemed.esc.api.shared.printer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class PrinterITest {

    private final FixtureService fixtureService;
    private final KinderdossierPrintService kinderdossierPrintService;

    @Test
    @TestAsAdminBS
    void testFullDossierPrint() {
        // ARRANGE
        Untersuchung untersuchungWithImpfungen = fixtureService.createUntersuchungWithImpfungen();
        Assertions.assertThat(untersuchungWithImpfungen.getUntersuchungImpfungen()).isNotNull();
        var impfung =
            fixtureService.createInterneImpfungForUntersuchung(untersuchungWithImpfungen.getUntersuchungImpfungen());
        Assertions.assertThat(impfung).isNotNull();
        ID<Kind> kindId = untersuchungWithImpfungen.getEinladung().getDossier().getKind().getId();
        ID<Anlass> anlassId = untersuchungWithImpfungen.getEinladung().getAnlass().getId();
        // ACT
        PrintResultDTO printResultDTO = kinderdossierPrintService.printKinderdossier(
            KinderdossierPrintVariant.ANLAESSE,
            kindId,
            anlassId);
        // ASSERT
        assertThat(printResultDTO).isNotNull();

    }
}
