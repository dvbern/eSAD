package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NahvisusRechtsCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final NahvisusRechtsCsvColumn column = new NahvisusRechtsCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setNahvisusRechts(BigDecimal.valueOf(1500));
        assertThat(column.extract(einladung)).isEqualTo("1500");
    }
}
