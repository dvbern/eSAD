package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SpracheSprachverstaendnisCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SpracheSprachverstaendnisCsvColumn column = new SpracheSprachverstaendnisCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSprache untersuchungSprache = new UntersuchungSprache();
        untersuchung.setUntersuchungSprache(untersuchungSprache);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSprache.setSprachverstaendnis(SingleValueEnhanced.of(Sprachverstaendnis.NICHT_BESTANDEN, null));

        when(tlMock.translateValue(Sprachverstaendnis.NICHT_BESTANDEN)).thenReturn("Nicht Okay");
        assertThat(column.extract(einladung)).isEqualTo("Nicht Okay");
    }
}

