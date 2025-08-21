package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NahvisusLinksCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final NahvisusLinksCsvColumn column = new NahvisusLinksCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setNahvisusLinks(BigDecimal.valueOf(100));
        assertThat(column.extract(einladung)).isEqualTo("100");
    }
}
