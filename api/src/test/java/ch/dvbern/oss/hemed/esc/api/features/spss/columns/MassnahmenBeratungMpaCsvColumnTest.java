package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MassnahmenBeratungMpaCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractCsv() {
        // ARRANGE
        final MassnahmenBeratungMpaCsvColumn column =
            new MassnahmenBeratungMpaCsvColumn(configForCsv);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");

        untersuchungMassnahmen.setBeratungMPA(false);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        untersuchungMassnahmen.setBeratungMPA(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }

}
