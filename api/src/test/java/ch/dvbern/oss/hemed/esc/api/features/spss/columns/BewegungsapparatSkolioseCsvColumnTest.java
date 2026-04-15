package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BewegungsapparatSkolioseCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final BewegungsapparatSkolioseCsvColumn column = new BewegungsapparatSkolioseCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungBewegungsapparat untersuchungBewegungsapparat = new UntersuchungBewegungsapparat();
        untersuchung.setUntersuchungBewegungsapparat(untersuchungBewegungsapparat);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(Skoliose.SKOLIOSE_NEIN)).thenReturn("Nein");

        untersuchungBewegungsapparat.setSkoliose(SingleValueEnhanced.of(Skoliose.SKOLIOSE_NEIN, null));
        assertThat(column.extract(einladung)).isEqualTo("Nein");
    }
}
