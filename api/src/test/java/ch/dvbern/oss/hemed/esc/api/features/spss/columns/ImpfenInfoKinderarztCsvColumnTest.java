package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfenInfoKinderarztCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfenInfoKinderarztCsvColumn column = new ImpfenInfoKinderarztCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Anlass anlass = new Anlass();
        einladung.setAnlass(anlass);

        // empty value
        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen = new UntersuchungImpfungenMassnahmen();
        untersuchungImpfungen.setUntersuchungImpfungenMassnahmen(untersuchungImpfungenMassnahmen);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungImpfungenMassnahmen.setInformationHausarzt(false);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungImpfungenMassnahmen.setInformationHausarzt(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
