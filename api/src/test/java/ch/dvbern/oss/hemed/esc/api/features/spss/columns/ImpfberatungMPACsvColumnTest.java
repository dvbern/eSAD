package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfberatungMPACsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfberatungMPACsvColumn column = new ImpfberatungMPACsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen = new UntersuchungImpfungenMassnahmen();
        untersuchungImpfungen.setUntersuchungImpfungenMassnahmen(untersuchungImpfungenMassnahmen);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungImpfungenMassnahmen.setImpfberatungMpaDurchgefuehrt(false);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungImpfungenMassnahmen.setImpfberatungMpaDurchgefuehrt(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
