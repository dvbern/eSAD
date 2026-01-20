package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallFangen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BallFangenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final BallFangenCsvColumn column = new BallFangenCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(BallFangen.BALL_NICHT_GEFANGEN)).thenReturn("Ball nicht genfange");

        untersuchungArzt.setBallFangen(BallFangen.BALL_NICHT_GEFANGEN);
        assertThat(column.extract(einladung)).isEqualTo("Ball nicht genfange");
    }
}
