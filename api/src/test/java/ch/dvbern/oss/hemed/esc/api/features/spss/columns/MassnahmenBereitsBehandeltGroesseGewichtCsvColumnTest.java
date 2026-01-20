package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MassnahmenBereitsBehandeltGroesseGewichtCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenBereitsBehandeltGroesseGewichtCsvColumn column =
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungMassnahmen.setBereitsBehandeltGroesseGewicht(true);

        assertThat(column.extract(einladung)).isEqualTo("1");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final MassnahmenBereitsBehandeltGroesseGewichtCsvColumn column =
            new MassnahmenBereitsBehandeltGroesseGewichtCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungMassnahmen.setBereitsBehandeltGroesseGewicht(true);
        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
