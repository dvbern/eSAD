package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LesefitPunkteCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractCsv() {
        // ARRANGE
        final LesefitPunkteCsvColumn column = new LesefitPunkteCsvColumn(configForCsv);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setLesefitPunkte(new BigDecimal("21.0"));
        assertThat(column.extract(einladung)).isEqualTo("21.0");
    }
}
