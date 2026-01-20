package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ImpfausweisUnvollstaendingCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        final ImpfausweisUnvollstaendingCsvColumn column = new ImpfausweisUnvollstaendingCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisUnvollstaendig -> is not checked
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setImpfausweisUnvollstaendig(false);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisNichtVorhanden -> is checked
        untersuchungImpfungen.setImpfausweisUnvollstaendig(true);
        assertThat(column.extract(einladung)).isEqualTo("1");
    }

    @Test
    void extractorCsv() {
        // ARRANGE
        final ImpfausweisUnvollstaendingCsvColumn column = new ImpfausweisUnvollstaendingCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisUnvollstaendig -> is not checked
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setImpfausweisUnvollstaendig(false);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // ImpfausweisNichtVorhanden -> is checked
        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");

        untersuchungImpfungen.setImpfausweisUnvollstaendig(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }

}
