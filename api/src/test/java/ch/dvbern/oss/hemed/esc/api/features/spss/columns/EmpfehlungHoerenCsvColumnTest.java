package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EmpfehlungHoerenCsvColumnTest extends AbstractBaseColumnTest {
    @Test
    void extract() {
        // ARRANGE
        final EmpfehlungHoerenCsvColumn column =
            new EmpfehlungHoerenCsvColumn(configForCsv, HoerenEmpfehlung.UEBERPRUEFUNG_ARZT);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm = new UntersuchungHoerenAudiogramm();
        untersuchung.setUntersuchungHoerenAudiogramm(untersuchungHoerenAudiogramm);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungHoerenAudiogramm.setEmpfehlung(MultiValueEnhanced.of(EnumSet.of(HoerenEmpfehlung.UEBERPRUEFUNG_ARZT), null));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
