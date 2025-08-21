package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspektionCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final InspektionCsvColumn column = new InspektionCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setInspektion(InspektionKonvergenzResultat.UNAUFFAELLIG);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehen.setInspektion(InspektionKonvergenzResultat.AUFFAELLIG);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehen.setInspektion(InspektionKonvergenzResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("3");
    }
}
