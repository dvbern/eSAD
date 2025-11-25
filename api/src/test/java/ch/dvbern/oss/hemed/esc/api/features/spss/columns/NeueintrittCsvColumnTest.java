package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NeueintrittCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final NeueintrittCsvColumn column = new NeueintrittCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> null
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungDurchfuehrung -> null
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Neueintritt -> null
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        assertThat(column.extract(einladung)).isEmpty();

        // With Neueintritt, but false
        untersuchungDurchfuehrung.setNeueintritt(false);
        assertThat(column.extract(einladung)).isEmpty();

        // With Neueintritt true -> 1
        untersuchungDurchfuehrung.setNeueintritt(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
