package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrabismusCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(StrabismusResultat.class)
    void extract(StrabismusResultat resultat) {
        // ARRANGE
        final StrabismusCsvColumn column = new StrabismusCsvColumn(config, null, resultat);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        // Check extract return empty value if strabismus list does not have the selected element
        if (resultat == StrabismusResultat.EXO) {
            untersuchungSehenBeurteilung.setStrabismus(MultiValueEnhanced.of(EnumSet.of(StrabismusResultat.ESO), null));
            assertThat(column.extract(einladung)).isEmpty();
        } else {
            untersuchungSehenBeurteilung.setStrabismus(MultiValueEnhanced.of(EnumSet.of(StrabismusResultat.EXO), null));
            assertThat(column.extract(einladung)).isEmpty();
        }

        untersuchungSehenBeurteilung.setStrabismus(MultiValueEnhanced.of(EnumSet.of(resultat), null));
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
