package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    void extractorCsv() {
        // ARRANGE
        final KeinImpfausweisIsNotCheckedCsvColumn column = new KeinImpfausweisIsNotCheckedCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // with ImpfausweisNichtVorhanden -> is checked
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");
        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");

        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setImpfausweisNichtVorhanden(true);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // without ImpfausweisNichtVorhanden -> is not checked
        untersuchungImpfungen.setImpfausweisNichtVorhanden(false);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
