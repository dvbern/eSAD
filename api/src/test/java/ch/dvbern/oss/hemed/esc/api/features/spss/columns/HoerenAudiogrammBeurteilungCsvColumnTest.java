package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
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

        untersuchungHoeren.setAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.values()));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
