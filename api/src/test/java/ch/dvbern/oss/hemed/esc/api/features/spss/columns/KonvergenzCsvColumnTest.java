package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KonvergenzCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final KonvergenzCsvColumn column = new KonvergenzCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setKonvergenz(InspektionKonvergenzResultat.AUFFAELLIG);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setKonvergenz(InspektionKonvergenzResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("3");
    }
}
