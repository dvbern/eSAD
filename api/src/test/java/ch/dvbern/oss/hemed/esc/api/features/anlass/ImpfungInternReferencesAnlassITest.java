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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.person.RemoveKindService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@RequiredArgsConstructor
class ImpfungInternReferencesAnlassITest extends AbstractITest {

    private final FixtureService fixtureService;
    private final EinladungService einladungService;
    private final AnlassService anlassService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final RemoveKindService removeKindService;

    @Test
    @TestAsAdminBS
    void delete_einladung_or_anlass_when_impfungen_from_other_anlass_reference_anlass() {
        // ARRANGE: Kind mit 3 Anlässen, Impfung im ersten erstellt, in weiteren Anlässen kopiert,
        // im zweiten Anlass durchgefuehrt
        Dossier dossier = fixtureService.createDossier();

        // Anlass 1: Impfung wird erstellt
        Anlass anlass1 = fixtureService.createAnlass(LocalDate.now().minusMonths(3));
        Einladung einladung1 = fixtureService.createEinladung(anlass1, dossier);
        Untersuchung untersuchung1 = fixtureService.createUntersuchungWithImpfungen(einladung1);
        UntersuchungImpfungen impfungen1 = untersuchung1.getUntersuchungImpfungen();
        assertThat(impfungen1).isNotNull();
        // Impfung im Anlass 1 erstellt: anlassImpfungErfasst = anlass1
        UntersuchungImpfungIntern impfungAnlass1 = fixtureService.createUntersuchungImpfungIntern(impfungen1);
        assertThat(impfungAnlass1.getAnlassImpfungErfasst()).isEqualTo(anlass1);

        // Anlass 2: Impfung wird übernommen (kopiert) - anlassImpfungErfasst zeigt immer noch auf Anlass 1
        Anlass anlass2 = fixtureService.createAnlass(LocalDate.now().minusMonths(2));
        Einladung einladung2 = fixtureService.createEinladung(anlass2, dossier);
        Untersuchung untersuchung2 = fixtureService.createUntersuchung(einladung2.getId());
        List<UntersuchungImpfungIntern> impfungen2 =
            untersuchungImpfungInternService.findByUntersuchung(untersuchung2.getId());
        assertThat(impfungen2).hasSize(1);
        UntersuchungImpfungIntern kopierteImpfungAnlass2 = impfungen2.getFirst();
        // Impfung wird durchgeführt
        kopierteImpfungAnlass2 = fixtureService.impfungDurchfuehren(kopierteImpfungAnlass2.getId(), anlass2);
        // Kopierte Impfung: anlassImpfungErfasst zeigt auf anlass1 (wo sie erstellt wurde)
        assertThat(kopierteImpfungAnlass2.getAnlassImpfungErfasst()).isEqualTo(anlass1);
        assertThat(kopierteImpfungAnlass2.getAnlassImpfungDurchgefuehrt()).isEqualTo(anlass2);

        // Anlass 3: Impfung wird wiederum übernommen (kopiert)
        Anlass anlass3 = fixtureService.createAnlass(LocalDate.now().minusMonths(1));
        Einladung einladung3 = fixtureService.createEinladung(anlass3, dossier);
        Untersuchung untersuchung3 = fixtureService.createUntersuchung(einladung3.getId());
        List<UntersuchungImpfungIntern> impfungen3 =
            untersuchungImpfungInternService.findByUntersuchung(untersuchung3.getId());
        assertThat(impfungen3).hasSize(1);
        UntersuchungImpfungIntern kopierteImpfungAnlass3 = impfungen3.getFirst();
        assertThat(kopierteImpfungAnlass3.getAnlassImpfungErfasst()).isEqualTo(anlass1);
        assertThat(kopierteImpfungAnlass3.getAnlassImpfungDurchgefuehrt()).isEqualTo(anlass2);

        // Löschen der Einladung von Anlass 1 scheitert, da kopierte Impfungen
        // in Anlass 2 und 3 noch anlassImpfungErfasst auf Anlass 1 referenzieren.
        // removeEinladung löscht zwar die Impfungen von Untersuchung 1, aber nicht die kopierten
        // Impfungen in Untersuchung 2 und 3. Wenn dann der (nun leere) Anlass 1 gelöscht wird,
        // scheitert es an den FK-Constraints.
        // Wir haben aktuell entschieden, dies so zu lassen, da es eigentlich nicht vorkommen sollte
        // Siehe ESC-1103, Problem 1

        // ACT & ASSERT

        // Einladung löschen inkl. löschen leerer Anlässe schlägt fehl, da Anlass in verbleibenden
        // Impfungen referenziert
        assertThatThrownBy(() -> einladungService.removeEinladung(einladung1.getId(), true))
            .hasStackTraceContaining("fk_untersuchungimpfungintern_anlass");
        // Einladung löschen, ohne den Anlass zu löschen geht:
        assertThatNoException().isThrownBy(() -> einladungService.removeEinladung(einladung1.getId(), false));
        // Anlass löschen geht nicht, da referenziert
        assertThatThrownBy(() -> anlassService.remove(anlass1.getId()))
            .hasStackTraceContaining("fk_untersuchungimpfungintern_anlass");
        // Kind löschen hingegen geht, da wir dort ALLE Impfungen & Anlässe löschen, es also keine
        // Referenzen mehr geben kann
        assertThatNoException().isThrownBy(() -> removeKindService.remove(dossier.getKind().getId()));
    }
}
