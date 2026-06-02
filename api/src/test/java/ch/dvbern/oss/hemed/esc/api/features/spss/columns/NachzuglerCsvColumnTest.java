package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    @Test
    void extractCsv() {
        // ARRANGE
        final NachzuglerCsvColumn column = new NachzuglerCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");

        // without Untersuchung -> null
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungDurchfuehrung -> null
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Nachzuegler -> null
        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // With Nachzuegler, but false
        untersuchungDurchfuehrung.setNachzuegler(false);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // With Nachzuegler true -> 1
        untersuchungDurchfuehrung.setNachzuegler(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
