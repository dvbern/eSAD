package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MassnahmenNachkontrolleSelectedCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenNachkontrolleSelectedCsvColumn column =
            new MassnahmenNachkontrolleSelectedCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        AnlassFlags anlassFlags = new AnlassFlags();
        untersuchungMassnahmen.setNachkontrolle(anlassFlags);

        assertThat(column.extract(einladung)).isEmpty();

        anlassFlags.setNachkontrolleSehen(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        anlassFlags.setNachkontrolleHoeren(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        anlassFlags.setNachkontrolleGroesseGewicht(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        anlassFlags.setNachkontrolleSprache(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        anlassFlags.setNachkontrolleMotorik(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        anlassFlags.setNachkontrolleAnderes(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseNachkontrolle(anlassFlags);
        assertThat(column.extract(einladung)).isEmpty();
    }

    private void setToFalseNachkontrolle(AnlassFlags anlassFlags) {
        anlassFlags.setNachkontrolleSehen(false);
        anlassFlags.setNachkontrolleHoeren(false);
        anlassFlags.setNachkontrolleGroesseGewicht(false);
        anlassFlags.setNachkontrolleSprache(false);
        anlassFlags.setNachkontrolleMotorik(false);
        anlassFlags.setNachkontrolleAnderes(false);
    }
}
