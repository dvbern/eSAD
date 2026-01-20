package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MotorikCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @MethodSource("ch.dvbern.oss.hemed.esc.api.testutil.spss.SpssTestUtil#MotorikTestCases")
    void extract(Motorik motorik, String expected) {
        // ARRANGE
        final MotorikCsvColumn column =
            new MotorikCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setMotorik(motorik);

        assertThat(column.extract(einladung)).isEqualTo(expected);
    }

    @Test
    void extract() {
        // ARRANGE
        Motorik motorik = Motorik.AUFFAELLIG;
        final MotorikCsvColumn column =
            new MotorikCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setMotorik(motorik);
        when(tlMock.translateValue(Motorik.AUFFAELLIG)).thenReturn("Auffällig");
        assertThat(column.extract(einladung)).isEqualTo("Auffällig");
    }
}
