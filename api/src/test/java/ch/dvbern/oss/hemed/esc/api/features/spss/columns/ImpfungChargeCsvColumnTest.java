package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfungChargeCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungChargeCsvColumn column = new ImpfungChargeCsvColumn(configForCsv);

        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now());

        // Impfung ohne Charge
        assertThat(column.extract(untersuchungImpfungIntern)).isEmpty();

        // Charge
        ImpfstoffCharge charge = ImpfstoffChargeBuilder.create(builder ->
            builder.withName("Test Charge"));

        untersuchungImpfungIntern.setCharge(charge);
        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Test Charge");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungChargeCsvColumn column = new ImpfungChargeCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
