package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class EinbeinstandCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @MethodSource("ch.dvbern.oss.hemed.esc.api.testutil.spss.SpssTestUtil#einbeinstandTestCases")
    void extract(Einbeinstand einbeinstand, String expected) {
        // ARRANGE
        final EinbeinstandCsvColumn column =
            new EinbeinstandCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setEinbeinstand(einbeinstand);

        assertThat(column.extract(einladung)).isEqualTo(expected);
    }
}
