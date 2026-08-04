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

package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.util.List;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AutoAufgabeServiceTest {

    private final AufgabeService aufgabeService = Mockito.mock(AufgabeService.class);
    private final BenutzerService benutzerService = Mockito.mock(BenutzerService.class);
    private final LabelService labelService = Mockito.mock(LabelService.class);
    private final ConfigService configService = Mockito.mock(ConfigService.class);
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    // TL that echoes the I18nMessage as a string — lets us assert no "null" leaks into the composed titel
    private final AutoAufgabeService service = new AutoAufgabeService(
        aufgabeService,
        benutzerService,
        labelService,
        configService,
        Record::toString,
        appClock
    );

    @Test
    void createAufgabeGruppenFolgetermin_titel_present_no_null_in_aufgabe_titel() {
        // ARRANGE
        Benutzer mpa = BenutzerBuilder.defaultBenutzer();
        Anlass anlass = AnlassBuilder.create(b -> b.withTitle("Mein Anlass").withSchuljahr(2025).withMPA(mpa));
        Mockito.when(aufgabeService.findAufgabenForAnlassAndLabel(any(), any())).thenReturn(List.of());
        Mockito.when(labelService.getById(any())).thenReturn(Mockito.mock(Label.class));
        Mockito.when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(mpa);
        ArgumentCaptor<Aufgabe> captor = ArgumentCaptor.forClass(Aufgabe.class);
        Mockito.when(aufgabeService.persist(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // ACT
        service.createAufgabeGruppenFolgetermin(anlass);

        // ASSERT
        String titel = captor.getValue().getTitel();
        assertThat(titel).contains("Mein Anlass");
    }

    @Test
    void createAufgabeGruppenFolgetermin_titel_null_no_null_artifact_in_aufgabe_titel() {
        // ARRANGE
        Benutzer mpa = BenutzerBuilder.defaultBenutzer();
        Anlass anlass = AnlassBuilder.create(b -> b.withTitle(null).withSchuljahr(2025).withMPA(mpa));
        Mockito.when(aufgabeService.findAufgabenForAnlassAndLabel(any(), any())).thenReturn(List.of());
        Mockito.when(labelService.getById(any())).thenReturn(Mockito.mock(Label.class));
        Mockito.when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(mpa);
        ArgumentCaptor<Aufgabe> captor = ArgumentCaptor.forClass(Aufgabe.class);
        Mockito.when(aufgabeService.persist(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // ACT
        service.createAufgabeGruppenFolgetermin(anlass);

        // ASSERT
        String titel = captor.getValue().getTitel();
        assertThat(titel).doesNotContain("null");
    }

    @Test
    void createAufgabeGruppenFolgetermin_titel_blank_no_null_artifact_in_aufgabe_titel() {
        // ARRANGE
        Benutzer mpa = BenutzerBuilder.defaultBenutzer();
        Anlass anlass = AnlassBuilder.create(b -> b.withTitle("   ").withSchuljahr(2025).withMPA(mpa));
        Mockito.when(aufgabeService.findAufgabenForAnlassAndLabel(any(), any())).thenReturn(List.of());
        Mockito.when(labelService.getById(any())).thenReturn(Mockito.mock(Label.class));
        Mockito.when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(mpa);
        ArgumentCaptor<Aufgabe> captor = ArgumentCaptor.forClass(Aufgabe.class);
        Mockito.when(aufgabeService.persist(captor.capture())).thenAnswer(i -> i.getArgument(0));

        // ACT
        service.createAufgabeGruppenFolgetermin(anlass);

        // ASSERT
        String titel = captor.getValue().getTitel();
        assertThat(titel).doesNotContain("null");
    }
}
