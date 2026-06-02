package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GrobmotorikEinbeinhuepfenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final GrobmotorikEinbeinhuepfenCsvColumn column = new GrobmotorikEinbeinhuepfenCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung = new UntersuchungSchulrelevanteBeeintraechtigung();
        untersuchung.setUntersuchungSchulrelevanteBeeintraechtigung(beeintraechtigung);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(GrobmotorikEinbeinhuepfen.BESTANDEN)).thenReturn("Bestanden");

        beeintraechtigung.setGrobmotorikEinbeinhuepfen(SingleValueEnhanced.of(GrobmotorikEinbeinhuepfen.BESTANDEN, null));
        assertThat(column.extract(einladung)).isEqualTo("Bestanden");
    }
}
