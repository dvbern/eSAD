package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SprachfaehigkeitMultiCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(Sprachfaehigkeit.class)
    void extract(Sprachfaehigkeit sprachfaehigkeit) {
        // ARRANGE
        final SprachfaehigkeitMultiCsvColumn column = new SprachfaehigkeitMultiCsvColumn(config, null, sprachfaehigkeit);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSet.allOf(Sprachfaehigkeit.class));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        Sprachfaehigkeit sprachfaehigkeit = Sprachfaehigkeit.ARTIKULATIONSFEHLER;
        final SprachfaehigkeitMultiCsvColumn
				column = new SprachfaehigkeitMultiCsvColumn(configForCsv, null, sprachfaehigkeit);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        untersuchungArzt.setSprachfaehigkeitMultiValue(EnumSet.allOf(Sprachfaehigkeit.class));

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }

}
