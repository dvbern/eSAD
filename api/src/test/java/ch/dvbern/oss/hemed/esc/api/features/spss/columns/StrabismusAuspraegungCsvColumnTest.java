package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrabismusAuspraegungCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final StrabismusAuspraegungCsvColumn column = new StrabismusAuspraegungCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(SingleValueEnhanced.of(StrabismusAuspraegungResultat.KEIN_STRABISMUS, null));
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(SingleValueEnhanced.of(StrabismusAuspraegungResultat.MANIFEST, null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(SingleValueEnhanced.of(StrabismusAuspraegungResultat.LATENT, null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(SingleValueEnhanced.of(StrabismusAuspraegungResultat.INTERMITTIEREND, null));
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(SingleValueEnhanced.of(StrabismusAuspraegungResultat.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
