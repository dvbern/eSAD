package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class HoerenAudiogrammBeurteilungCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(AudiogrammBeurteilung.class)
    void extract(AudiogrammBeurteilung audiogrammBeurteilung) {
        // ARRANGE
        final HoerenBeurteilungCsvColumn column = new HoerenBeurteilungCsvColumn(config, null, audiogrammBeurteilung);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungHoerenAudiogramm untersuchungHoeren = new UntersuchungHoerenAudiogramm();
        untersuchung.setUntersuchungHoerenAudiogramm(untersuchungHoeren);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungHoeren.setAudiogrammBeurteilung(MultiValueEnhanced.of(EnumSet.allOf(AudiogrammBeurteilung.class), null));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
