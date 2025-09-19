package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
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

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(StrabismusAuspraegungResultat.KEIN_STRABISMUS);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(StrabismusAuspraegungResultat.MANIFEST);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(StrabismusAuspraegungResultat.LATENT);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(StrabismusAuspraegungResultat.INTERMITTIEREND);
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setStrabismusAuspraegung(StrabismusAuspraegungResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
