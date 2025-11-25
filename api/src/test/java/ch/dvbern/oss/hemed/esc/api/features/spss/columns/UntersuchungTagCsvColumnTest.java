package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UntersuchungTagCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final UntersuchungTagCsvColumn column = new UntersuchungTagCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Anlass anlass = new Anlass();
        anlass.setDatum(LocalDate.of(2024, 4, 20));
        einladung.setAnlass(anlass);

        assertThat(column.extract(einladung)).isEqualTo("20");
    }
}
