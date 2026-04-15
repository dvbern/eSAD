package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BrilleLinsenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final BrilleLinsenCsvColumn column = new BrilleLinsenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setBrilleLinsen(SingleValueEnhanced.of(BrilleLinsenResultat.OHNE_KEINE, null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setBrilleLinsen(SingleValueEnhanced.of(BrilleLinsenResultat.MIT, null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setBrilleLinsen(SingleValueEnhanced.of(BrilleLinsenResultat.OHNE_VERGESSEN, null));
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setBrilleLinsen(SingleValueEnhanced.of(BrilleLinsenResultat.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("4");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final BrilleLinsenCsvColumn column = new BrilleLinsenCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(BrilleLinsenResultat.OHNE_KEINE)).thenReturn("Keine Brille");
        untersuchungSehenBeurteilung.setBrilleLinsen(SingleValueEnhanced.of(BrilleLinsenResultat.OHNE_KEINE, null));
        assertThat(column.extract(einladung)).isEqualTo("Keine Brille");
    }
}
