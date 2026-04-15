package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfungDatumCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungDatumCsvColumn column = new ImpfungDatumCsvColumn(configForCsv);

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now());

        // Impfung ohne Impfdatum
        assertThat(column.extract(untersuchungImpfungIntern)).isEmpty();

        // Impfdatum setzen
        untersuchungImpfungIntern.setDatumImpfung(LocalDate.of(2025, 5, 31));

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("31.05.2025");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungDatumCsvColumn column = new ImpfungDatumCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
