package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NachzuglerCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final NachzuglerCsvColumn column = new NachzuglerCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> null
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungDurchfuehrung -> null
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Nachzuegler -> null
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        assertThat(column.extract(einladung)).isEmpty();

        // With Nachzuegler, but false
        untersuchungDurchfuehrung.setNachzuegler(false);
        assertThat(column.extract(einladung)).isEmpty();

        // With Nachzuegler true -> 1
        untersuchungDurchfuehrung.setNachzuegler(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
