package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
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

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setStrabismusAuspraegung(StrabismusAuspraegungResultat.KEIN_STRABISMUS);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehen.setStrabismusAuspraegung(StrabismusAuspraegungResultat.MANIFEST);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehen.setStrabismusAuspraegung(StrabismusAuspraegungResultat.LATENT);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehen.setStrabismusAuspraegung(StrabismusAuspraegungResultat.INTERMITTIEREND);
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehen.setStrabismusAuspraegung(StrabismusAuspraegungResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
