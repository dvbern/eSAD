package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    @Test
    void extractCsv() {
        // ARRANGE
        final NachkontrolleCsvColumn column = new NachkontrolleCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");

        // without Untersuchung -> null
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungDurchfuehrung -> null
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Natchkontrolle -> null
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // With Nachkontrolle, but false
        untersuchungDurchfuehrung.setNachkontrolle(false);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // With Natchkontrolle true
        untersuchungDurchfuehrung.setNachkontrolle(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
