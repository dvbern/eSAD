package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        // Check that the contains works, by adding an incorrect element
        if (resultat == DistanzSehenResultat.NORMAL) {
            untersuchungSehenBeurteilung.setNahvisus(MultiValueEnhanced.of(EnumSet.of(DistanzSehenResultat.LINKS_REDUZIERT), null));
            assertThat(column.extract(einladung)).isEmpty();
        } else {
            untersuchungSehenBeurteilung.setNahvisus(MultiValueEnhanced.of(EnumSet.of(DistanzSehenResultat.NORMAL), null));
            assertThat(column.extract(einladung)).isEmpty();
        }

        untersuchungSehenBeurteilung.setNahvisus(MultiValueEnhanced.of(EnumSet.of(resultat), null));
        assertThat(column.extract(einladung)).isEqualTo("1");
    }

    @Test
    void extract() {
        // ARRANGE
        DistanzSehenResultat resultat = DistanzSehenResultat.LINKS_REDUZIERT;

        final NahvisusCsvColumn column = new NahvisusCsvColumn(configForCsv, null, resultat);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungSehenBeurteilung.setNahvisus(MultiValueEnhanced.of(EnumSet.of(resultat), null));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
