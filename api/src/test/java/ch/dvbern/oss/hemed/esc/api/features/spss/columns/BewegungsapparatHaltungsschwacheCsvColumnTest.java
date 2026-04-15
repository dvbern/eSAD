package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BewegungsapparatHaltungsschwacheCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final BewegungsapparatHaltungsschwacheCsvColumn column = new BewegungsapparatHaltungsschwacheCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungBewegungsapparat untersuchungBewegungsapparat = new UntersuchungBewegungsapparat();
        untersuchung.setUntersuchungBewegungsapparat(untersuchungBewegungsapparat);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(Haltungsschwaeche.NORMAL)).thenReturn("Normal");

        untersuchungBewegungsapparat.setHaltungsschwaeche(SingleValueEnhanced.of(Haltungsschwaeche.NORMAL, null));
        assertThat(column.extract(einladung)).isEqualTo("Normal");
    }
}
