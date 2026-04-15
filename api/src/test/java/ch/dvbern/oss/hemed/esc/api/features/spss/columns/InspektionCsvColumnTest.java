package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class InspektionCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final InspektionCsvColumn column = new InspektionCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setInspektion(SingleValueEnhanced.of(
            InspektionKonvergenzResultat.UNAUFFAELLIG,
            null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setInspektion(SingleValueEnhanced.of(
            InspektionKonvergenzResultat.AUFFAELLIG,
            null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setInspektion(SingleValueEnhanced.of(
            InspektionKonvergenzResultat.NICHT_GEPRUEFT,
            null));
        assertThat(column.extract(einladung)).isEqualTo("3");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final InspektionCsvColumn column = new InspektionCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(InspektionKonvergenzResultat.UNAUFFAELLIG)).thenReturn("Unauffällig");
        untersuchungSehenBeurteilung.setInspektion(SingleValueEnhanced.of(InspektionKonvergenzResultat.UNAUFFAELLIG, null));
        assertThat(column.extract(einladung)).isEqualTo("Unauffällig");
    }
}
