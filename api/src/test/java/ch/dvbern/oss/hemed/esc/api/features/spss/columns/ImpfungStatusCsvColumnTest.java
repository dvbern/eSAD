package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ImpfungStatusCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungStatusCsvColumn column = new ImpfungStatusCsvColumn(configForCsv);

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.create(builder -> builder.withImpfStatus(ImpfStatus.DURCHGEFUEHRT));

        when(tlMock.translateValue(ImpfStatus.DURCHGEFUEHRT)).thenReturn("Durchgefuehrt");

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Durchgefuehrt");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungStatusCsvColumn column = new ImpfungStatusCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
