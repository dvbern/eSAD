package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MpaAnlassCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MpaAnlassCsvColumn column = new MpaAnlassCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);

        // empty value
        assertThat(column.extract(einladung)).isEmpty();

        Benutzer mpa = new Benutzer();
        mpa.setVorname("Simone");
        mpa.setNachname("Niedermann");
        anlass.setMpa(mpa);

        assertThat(column.extract(einladung)).isEqualTo("Niedermann Simone");
    }
}
