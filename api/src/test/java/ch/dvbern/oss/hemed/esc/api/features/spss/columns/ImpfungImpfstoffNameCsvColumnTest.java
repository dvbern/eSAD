package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfungImpfstoffNameCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungImpfstoffNameCsvColumn column = new ImpfungImpfstoffNameCsvColumn(configForCsv);

        // Impfstoff
        Impfstoff impfstoff = ImpfstoffBuilder.create(builder -> builder.withName("Masern"));

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.create(builder -> builder.withImpfstoff(impfstoff));

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Masern");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungImpfstoffNameCsvColumn column = new ImpfungImpfstoffNameCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
