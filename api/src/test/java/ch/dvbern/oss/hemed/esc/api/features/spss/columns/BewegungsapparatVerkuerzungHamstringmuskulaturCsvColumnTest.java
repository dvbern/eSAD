package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn column = new BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungBewegungsapparat untersuchungBewegungsapparat = new UntersuchungBewegungsapparat();
        untersuchung.setUntersuchungBewegungsapparat(untersuchungBewegungsapparat);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(VerkuerzungHamstringmuskulatur.PATHOLOGISCH)).thenReturn("Pathologisch");

        untersuchungBewegungsapparat.setVerkuerzungHamstringmuskulatur(SingleValueEnhanced.of(VerkuerzungHamstringmuskulatur.PATHOLOGISCH, null));
        assertThat(column.extract(einladung)).isEqualTo("Pathologisch");
    }
}
