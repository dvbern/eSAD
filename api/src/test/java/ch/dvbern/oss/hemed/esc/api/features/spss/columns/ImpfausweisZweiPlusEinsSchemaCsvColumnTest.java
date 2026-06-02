package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfausweisZweiPlusEinsSchemaCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        final ImpfausweisZweiPlusEinsSchemaCsvColumn column = new ImpfausweisZweiPlusEinsSchemaCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisZweiPlusEinsSchema -> is not checked
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setImpfausweisZweiPlusEinsSchema(false);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisZweiPlusEinsSchema -> is checked
        untersuchungImpfungen.setImpfausweisZweiPlusEinsSchema(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
