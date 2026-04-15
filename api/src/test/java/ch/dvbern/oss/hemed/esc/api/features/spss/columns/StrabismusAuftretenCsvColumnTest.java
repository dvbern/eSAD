package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
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

        untersuchungSehenBeurteilung.setStrabismusAuftreten(SingleValueEnhanced.of(StrabismusAuftretenResultat.KEIN_STRABISMUS, null));
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungSehenBeurteilung.setStrabismusAuftreten(SingleValueEnhanced.of(StrabismusAuftretenResultat.RECHTS, null));
        assertThat(column.extract(einladung)).isEqualTo("1");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(SingleValueEnhanced.of(StrabismusAuftretenResultat.LINKS, null));
        assertThat(column.extract(einladung)).isEqualTo("2");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(SingleValueEnhanced.of(StrabismusAuftretenResultat.ALTERNNIEREND, null));
        assertThat(column.extract(einladung)).isEqualTo("3");

        untersuchungSehenBeurteilung.setStrabismusAuftreten(SingleValueEnhanced.of(StrabismusAuftretenResultat.NICHT_GEPRUEFT, null));
        assertThat(column.extract(einladung)).isEqualTo("4");

    }
}
