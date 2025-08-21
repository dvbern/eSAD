package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MassnahmenInformationElternCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenInformationElternCsvColumn column =
            new MassnahmenInformationElternCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungMassnahmen.setInformationEltern(InformationEltern.KEINE);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungMassnahmen.setInformationEltern(InformationEltern.BRIEF);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungMassnahmen.setInformationEltern(InformationEltern.TELEFON);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
