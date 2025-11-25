package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BMIWertCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final BMIWertCsvColumn column = new BMIWertCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungGroesseGewicht untersuchungGroesseGewicht = new UntersuchungGroesseGewicht();
        untersuchung.setUntersuchungGroesseGewicht(untersuchungGroesseGewicht);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungGroesseGewicht.setBmi(BigDecimal.valueOf(150));

        assertThat(column.extract(einladung)).isEqualTo("150");
    }
}
