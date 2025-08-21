package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FarbsehenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final FarbsehenCsvColumn column = new FarbsehenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setFarbsehen(FarbsehenResultat.NORMAL);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehen.setFarbsehen(FarbsehenResultat.PATHOLOGISCH);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehen.setFarbsehen(FarbsehenResultat.UNSICHER);
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehen.setFarbsehen(FarbsehenResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
