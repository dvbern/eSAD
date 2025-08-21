package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AnlassTypCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final AnlassTypCsvColumn column = new AnlassTypCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);

        AnlassConfig anlassConfig = new AnlassConfig();
        anlass.setAnlassConfig(anlassConfig);

        when(tlMock.translateValue(AnlassTyp.KINDERGARTEN)).thenReturn("SU KG");

        anlassConfig.setAnlassTyp(AnlassTyp.KINDERGARTEN);
        assertThat(column.extract(einladung)).isEqualTo("SU KG");
    }
}
