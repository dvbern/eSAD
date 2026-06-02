package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BMIBeurteilungCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @MethodSource("ch.dvbern.oss.hemed.esc.api.testutil.spss.SpssTestUtil#perzentilBmiTestCases")
    void extract(PerzentileBmi perzentileBmi, String expected) {
        // ARRANGE
        final BMIBeurteilungCsvColumn column =
            new BMIBeurteilungCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungGroesseGewicht untersuchungGroesseGewicht = new UntersuchungGroesseGewicht();
        untersuchung.setUntersuchungGroesseGewicht(untersuchungGroesseGewicht);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungGroesseGewicht.setBmiPerzentile(SingleValueEnhanced.of(perzentileBmi, null));

        assertThat(column.extract(einladung)).isEqualTo(expected);
    }

    @Test
    void extract() {
        // ARRANGE
        final BMIBeurteilungCsvColumn column =
            new BMIBeurteilungCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungGroesseGewicht untersuchungGroesseGewicht = new UntersuchungGroesseGewicht();
        untersuchung.setUntersuchungGroesseGewicht(untersuchungGroesseGewicht);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(PerzentileBmi.NORMAL)).thenReturn("BMI Normal");
        untersuchungGroesseGewicht.setBmiPerzentile(SingleValueEnhanced.of(PerzentileBmi.NORMAL, null));
        assertThat(column.extract(einladung)).isEqualTo("BMI Normal");
    }
}
