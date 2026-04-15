package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ImpfungImpfstoffKrankheitenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungImpfstoffKrankheitenCsvColumn column = new ImpfungImpfstoffKrankheitenCsvColumn(configForCsv);

        // Impfstoff
        Impfstoff impfstoff = ImpfstoffBuilder.create(builder -> builder.forKrankheiten(Krankheit.MASERN, Krankheit.MUMPS));

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.create(builder -> builder.withImpfstoff(impfstoff));

        when(tlMock.translateValue(Krankheit.MASERN)).thenReturn("Masern");
        when(tlMock.translateValue(Krankheit.MUMPS)).thenReturn("Mumps");

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Masern,Mumps");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungImpfstoffKrankheitenCsvColumn column = new ImpfungImpfstoffKrankheitenCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
