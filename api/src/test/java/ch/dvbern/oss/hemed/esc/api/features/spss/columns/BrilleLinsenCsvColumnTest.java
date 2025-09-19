package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrilleLinsenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final BrilleLinsenCsvColumn column = new BrilleLinsenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setBrilleLinsen(BrilleLinsenResultat.OHNE_KEINE);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setBrilleLinsen(BrilleLinsenResultat.MIT);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setBrilleLinsen(BrilleLinsenResultat.OHNE_VERGESSEN);
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setBrilleLinsen(BrilleLinsenResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
