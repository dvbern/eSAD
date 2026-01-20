package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EmpfehlungSehenCsvColumnTest extends AbstractBaseColumnTest {
    @Test
    void extract() {
        // ARRANGE
        final EmpfehlungSehenCsvColumn column =
            new EmpfehlungSehenCsvColumn(configForCsv, SehenEmpfehlung.UEBERPRUEFUNG_AUGENARZT);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungSehenBeurteilung untersuchungSehen = new UntersuchungSehenBeurteilung();
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehen);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungSehen.setEmpfehlung(EnumSet.of(SehenEmpfehlung.UEBERPRUEFUNG_AUGENARZT));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
