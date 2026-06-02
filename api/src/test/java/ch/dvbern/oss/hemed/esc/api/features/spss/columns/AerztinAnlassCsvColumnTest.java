package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AerztinAnlassCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final AerztinAnlassCsvColumn column = new AerztinAnlassCsvColumn(config);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);

        // empty value
        assertThat(column.extract(einladung)).isEmpty();

        Benutzer arzt = new Benutzer();
        arzt.setVorname("Simone");
        arzt.setNachname("Niedermann");
        anlass.setArzt(arzt);

        assertThat(column.extract(einladung)).isEqualTo("Niedermann Simone");
    }
}
