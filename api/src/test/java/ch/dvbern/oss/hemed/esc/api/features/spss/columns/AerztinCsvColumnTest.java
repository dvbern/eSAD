package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AerztinCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final AerztinCsvColumn column = new AerztinCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);

        // empty value
        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungDurchfuehrung untersuchungDurchfuehrung = new UntersuchungDurchfuehrung();
        untersuchung.setUntersuchungDurchfuehrung(untersuchungDurchfuehrung);

        assertThat(column.extract(einladung)).isEmpty();

        Benutzer arzt = new Benutzer();
        arzt.setVorname("Simone");
        arzt.setNachname("Niedermann");
        untersuchungDurchfuehrung.setArzt(arzt);

        assertThat(column.extract(einladung)).isEqualTo("Niedermann Simone");
    }
}
