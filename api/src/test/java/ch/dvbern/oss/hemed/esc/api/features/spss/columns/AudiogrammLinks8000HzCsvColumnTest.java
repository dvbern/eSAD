package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class AudiogrammLinks8000HzCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @MethodSource("ch.dvbern.oss.hemed.esc.api.testutil.spss.SpssTestUtil#dezibelTestCases")
    void extract(Dezibel dezibel, String expected) {
        // ARRANGE
        final AudiogrammLinks8000HzCsvColumn column = new AudiogrammLinks8000HzCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungHoerenAudiogramm hoerenAudiogramm = new UntersuchungHoerenAudiogramm();
        untersuchung.setUntersuchungHoerenAudiogramm(hoerenAudiogramm);

        assertThat(column.extract(einladung)).isEmpty();

        Audiogramm audiogramm = new Audiogramm();
        hoerenAudiogramm.setAudiogrammLinks(audiogramm);

        audiogramm.setDbHz8000(dezibel);
        assertThat(column.extract(einladung)).isEqualTo(expected);
    }
}
