package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FeinmotorikCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final FeinmotorikCsvColumn column = new FeinmotorikCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung = new UntersuchungSchulrelevanteBeeintraechtigung();
        untersuchung.setUntersuchungSchulrelevanteBeeintraechtigung(beeintraechtigung);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(Feinmotorik.BESTANDEN)).thenReturn("Bestanden");

        beeintraechtigung.setFeinmotorik(SingleValueEnhanced.of(Feinmotorik.BESTANDEN, null));
        assertThat(column.extract(einladung)).isEqualTo("Bestanden");
    }
}
