package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeinImpfausweisIsNotCheckedCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        final KeinImpfausweisIsNotCheckedCsvColumn column = new KeinImpfausweisIsNotCheckedCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // with ImpfausweisNichtVorhanden -> is checked
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setImpfausweisNichtVorhanden(true);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        // without ImpfausweisNichtVorhanden -> is not checked
        untersuchungImpfungen.setImpfausweisNichtVorhanden(false);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
