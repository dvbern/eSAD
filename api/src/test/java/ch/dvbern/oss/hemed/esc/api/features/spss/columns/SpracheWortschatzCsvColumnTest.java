package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SpracheWortschatzCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SpracheWortschatzCsvColumn column = new SpracheWortschatzCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSprache untersuchungSprache = new UntersuchungSprache();
        untersuchung.setUntersuchungSprache(untersuchungSprache);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSprache.setWortschatz(SingleValueEnhanced.of(Wortschatz.NICHT_BESTANDEN, null));

        when(tlMock.translateValue(Wortschatz.NICHT_BESTANDEN)).thenReturn("Nicht Okay");
        assertThat(column.extract(einladung)).isEqualTo("Nicht Okay");
    }
}

