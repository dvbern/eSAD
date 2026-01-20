package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EinbeinhuepfenRechtsCsvColumnTest extends AbstractBaseColumnTest {
    @Test
    void extract() {
        // ARRANGE
        final EinbeinhuepfenRechtsCsvColumn column =
            new EinbeinhuepfenRechtsCsvColumn(configForCsv);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(Einbeinhuepfen.NICHT_MOEGLICH)).thenReturn("Nicht möglich");
        untersuchungArzt.setEinbeinhuepfenRechts(Einbeinhuepfen.NICHT_MOEGLICH);
        assertThat(column.extract(einladung)).isEqualTo("Nicht möglich");
    }

}
