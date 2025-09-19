package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrabismusAuftretenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final StrabismusAuftretenCsvColumn column = new StrabismusAuftretenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuftreten(StrabismusAuftretenResultat.KEIN_STRABISMUS);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuftreten(StrabismusAuftretenResultat.RECHTS);
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(StrabismusAuftretenResultat.LINKS);
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(StrabismusAuftretenResultat.ALTERNNIEREND);
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(StrabismusAuftretenResultat.NICHT_GEPRUEFT);
        assertThat(column.extract(einladung)).isEqualTo("4");

    }
}
