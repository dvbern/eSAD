package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriageBefundSuCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final TriageBefundSuCsvColumn column = new TriageBefundSuCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungTriage untersuchungTriage = new UntersuchungTriage();
        untersuchung.setUntersuchungTriage(untersuchungTriage);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setBefundSu(false);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setBefundSu(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
