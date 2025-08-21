package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NachkontrolleCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final NachkontrolleCsvColumn column = new NachkontrolleCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> null
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungDurchfuehrung -> null
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Natchkontrolle -> null
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        assertThat(column.extract(einladung)).isEmpty();

        // With Nachkontrolle, but false
        untersuchungDurchfuehrung.setNachkontrolle(false);
        assertThat(column.extract(einladung)).isEmpty();

        // With Natchkontrolle true
        untersuchungDurchfuehrung.setNachkontrolle(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
