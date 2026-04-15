package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setFarbsehen(SingleValueEnhanced.of(FarbsehenResultat.NORMAL, null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setFarbsehen(SingleValueEnhanced.of(FarbsehenResultat.PATHOLOGISCH, null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setFarbsehen(SingleValueEnhanced.of(FarbsehenResultat.UNSICHER, null));
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setFarbsehen(SingleValueEnhanced.of(FarbsehenResultat.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("4");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final FarbsehenCsvColumn column = new FarbsehenCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(FarbsehenResultat.NORMAL)).thenReturn("Farbsehen Normal");
        untersuchungSehenBeurteilung.setFarbsehen(SingleValueEnhanced.of(FarbsehenResultat.NORMAL, null));
        assertThat(column.extract(einladung)).isEqualTo("Farbsehen Normal");
    }
}
