package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FernvisusCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(DistanzSehenResultat.class)
    void extract(DistanzSehenResultat resultat) {
        // ARRANGE
        final FernvisusCsvColumn column = new FernvisusCsvColumn(config, null, resultat);
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
            untersuchungSehenBeurteilung.setFernvisus(EnumSet.of(DistanzSehenResultat.LINKS_REDUZIERT));
            assertThat(column.extract(einladung)).isEmpty();
        } else {
            untersuchungSehenBeurteilung.setFernvisus(EnumSet.of(DistanzSehenResultat.NORMAL));
            assertThat(column.extract(einladung)).isEmpty();
        }

        untersuchungSehenBeurteilung.setFernvisus(EnumSet.of(resultat));
        assertThat(column.extract(einladung)).isEqualTo("1");

    }

    @Test
    void extractCsv() {
        // ARRANGE
        final FernvisusCsvColumn column = new FernvisusCsvColumn(configForCsv, null, DistanzSehenResultat.LINKS_REDUZIERT);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungSehenBeurteilung.setFernvisus(EnumSet.of(DistanzSehenResultat.LINKS_REDUZIERT));
        assertThat(column.extract(einladung)).isEqualTo("Ja");

    }
}
