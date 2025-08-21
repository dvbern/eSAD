package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GewichtWertCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final GewichtWertCsvColumn column = new GewichtWertCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungGroesseGewicht untersuchungGroesseGewicht = new UntersuchungGroesseGewicht();
        untersuchung.setUntersuchungGroesseGewicht(untersuchungGroesseGewicht);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungGroesseGewicht.setGewicht(1600);

        assertThat(column.extract(einladung)).isEqualTo("1600");
    }
}
