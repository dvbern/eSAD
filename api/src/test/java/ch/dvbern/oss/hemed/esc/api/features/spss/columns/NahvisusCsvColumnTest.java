package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class NahvisusCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(DistanzSehenResultat.class)
    void extract(DistanzSehenResultat resultat) {
        // ARRANGE
        final NahvisusCsvColumn column = new NahvisusCsvColumn(config, null, resultat);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehen untersuchungSehen = new UntersuchungSehen();
        untersuchung.setUntersuchungSehen(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        // Check that the contains works, by adding an incorrect element
        if (resultat == DistanzSehenResultat.NORMAL) {
            untersuchungSehen.setNahvisus(List.of(DistanzSehenResultat.LINKS_REDUZIERT));
            assertThat(column.extract(einladung)).isEmpty();
        } else {
            untersuchungSehen.setNahvisus(List.of(DistanzSehenResultat.NORMAL));
            assertThat(column.extract(einladung)).isEmpty();
        }

        untersuchungSehen.setNahvisus(List.of(resultat));
        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
