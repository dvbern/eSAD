package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OhneStuetzradVelofahrenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {

        // ARRANGE
        final OhneStuetzradVelofahrenCsvColumn column = new OhneStuetzradVelofahrenCsvColumn(config);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung = new UntersuchungSchulrelevanteBeeintraechtigung();
        untersuchung.setUntersuchungSchulrelevanteBeeintraechtigung(beeintraechtigung);

        assertThat(column.extract(einladung)).isEmpty();
        when(tlMock.translateValue(OhneStuetzradVelofahren.JA_OHNE_STUETZRAD_VELOFAHREN)).thenReturn("Ja");

        beeintraechtigung.setOhneStuetzradVelofahren(SingleValueEnhanced.of(OhneStuetzradVelofahren.JA_OHNE_STUETZRAD_VELOFAHREN, null));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
