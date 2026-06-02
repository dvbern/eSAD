package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EinverstaendniserklaerungCsvColumnTest extends AbstractBaseColumnTest {
    @Test
    void extract() {
        // ARRANGE
        final EinverstaendniserklaerungCsvColumn column =
            new EinverstaendniserklaerungCsvColumn(configForCsv, Einverstaendniserklaerung.SCHWEIGEPFLICHT_VORHANDEN);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungArzt.setEinverstaendniserklaerung(EnumSet.of(Einverstaendniserklaerung.SCHWEIGEPFLICHT_VORHANDEN));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
