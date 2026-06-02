package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriageLehrpersonCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final TriageLehrpersonCsvColumn column = new TriageLehrpersonCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungTriage untersuchungTriage = new UntersuchungTriage();
        untersuchung.setUntersuchungTriage(untersuchungTriage);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setVorinformationLehrperson(false);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungTriage.setVorinformationLehrperson(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
