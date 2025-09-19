package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotilitaetCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MotilitaetCsvColumn column = new MotilitaetCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setMotilitaet(MotilitaetResultat.NORMAL);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setMotilitaet(MotilitaetResultat.PATHOLOGISCH);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setMotilitaet(MotilitaetResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("3");
    }
}
