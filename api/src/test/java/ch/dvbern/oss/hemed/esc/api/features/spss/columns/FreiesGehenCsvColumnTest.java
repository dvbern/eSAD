package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FreiesGehenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final FreiesGehenCsvColumn column = new FreiesGehenCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            new UntersuchungSchulrelevanteBeeintraechtigung();
        untersuchung.setUntersuchungSchulrelevanteBeeintraechtigung(beeintraechtigung);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(FreiesGehen.NICHT_GEPRUEFT)).thenReturn("Nicht geprüft");

        beeintraechtigung.setFreiesGehen(SingleValueEnhanced.of(FreiesGehen.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("Nicht geprüft");

        beeintraechtigung.setFreiesGehen(null);
        beeintraechtigung.setAnzahlMonateFreiesGehen(15);

        assertThat(column.extract(einladung)).isEqualTo("1 ³/₁₂");

        beeintraechtigung.setFreiesGehen(SingleValueEnhanced.of(FreiesGehen.NICHT_GEPRUEFT, null));
        beeintraechtigung.setAnzahlMonateFreiesGehen(24);

        assertThat(column.extract(einladung)).isEqualTo("2 ⁰/₁₂");
    }

}
