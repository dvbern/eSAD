package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TeilnahmestatusCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final TeilnahmestatusCsvColumn column = new TeilnahmestatusCsvColumn(configForCsv, null);
        Einladung einladung = new Einladung();
        einladung.setEinladungStatus(EinladungStatus.KEINE_ANMELDUNG);

        when(tlMock.translateValue(EinladungStatus.KEINE_ANMELDUNG)).thenReturn("Keine Anmeldung");
        assertThat(column.extract(einladung)).isEqualTo("Keine Anmeldung");
    }
}
