package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriageSuCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final TriageSuCsvColumn column = new TriageSuCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungTriage untersuchungTriage = new UntersuchungTriage();
        untersuchung.setUntersuchungTriage(untersuchungTriage);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setTriageSu(false);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setTriageSu(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
