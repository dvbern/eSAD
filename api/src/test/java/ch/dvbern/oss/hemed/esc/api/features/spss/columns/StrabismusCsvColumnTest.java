package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
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

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        // Check extract return empty value if strabismus list does not have the selected element
        if (resultat == StrabismusResultat.EXO) {
            untersuchungSehen.setStrabismus(List.of(StrabismusResultat.ESO));
            assertThat(column.extract(einladung)).isEmpty();
        } else {
            untersuchungSehen.setStrabismus(List.of(StrabismusResultat.EXO));
            assertThat(column.extract(einladung)).isEmpty();
        }

        untersuchungSehen.setStrabismus(List.of(resultat));
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
