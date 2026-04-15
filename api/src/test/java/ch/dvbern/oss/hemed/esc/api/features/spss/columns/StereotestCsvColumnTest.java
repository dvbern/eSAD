package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class StereotestCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final StereotestCsvColumn column = new StereotestCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStereotest(SingleValueEnhanced.of(StereotestResultat.NORMAL, null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setStereotest(SingleValueEnhanced.of(StereotestResultat.PATHOLOGISCH, null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setStereotest(SingleValueEnhanced.of(StereotestResultat.UNSICHER, null));
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setStereotest(SingleValueEnhanced.of(StereotestResultat.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("4");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final StereotestCsvColumn column = new StereotestCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(StereotestResultat.PATHOLOGISCH)).thenReturn("Pathologisch");
        untersuchungSehenBeurteilung.setStereotest(SingleValueEnhanced.of(StereotestResultat.PATHOLOGISCH, null));
        assertThat(column.extract(einladung)).isEqualTo("Pathologisch");
    }

}
