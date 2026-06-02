package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnlassDatumCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final AnlassDatumCsvColumn column = new AnlassDatumCsvColumn(config);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);
        anlass.setDatum(LocalDate.of(2025, 6, 27));

        assertThat(column.extract(einladung)).isEqualTo("27.06.2025");
    }
}
