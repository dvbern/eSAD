package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SpracheNachsprechenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SpracheNachsprechenCsvColumn column = new SpracheNachsprechenCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSprache untersuchungSprache = new UntersuchungSprache();
        untersuchung.setUntersuchungSprache(untersuchungSprache);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSprache.setNachsprechen(SingleValueEnhanced.of(Nachsprechen.NICHT_BESTANDEN, null));

        when(tlMock.translateValue(Nachsprechen.NICHT_BESTANDEN)).thenReturn("Nicht Okay");
        assertThat(column.extract(einladung)).isEqualTo("Nicht Okay");
    }
}
