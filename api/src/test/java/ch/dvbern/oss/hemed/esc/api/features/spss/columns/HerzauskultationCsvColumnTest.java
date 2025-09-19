package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class HerzauskultationCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @MethodSource("ch.dvbern.oss.hemed.esc.api.testutil.spss.SpssTestUtil#HerzauskultationTestCases")
    void extract(Herzauskultation herzauskultation, String expected) {
        // ARRANGE
        final HerzauskultationCsvColumn column =
            new HerzauskultationCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setHerzauskultation(herzauskultation);

        assertThat(column.extract(einladung)).isEqualTo(expected);
    }
}
