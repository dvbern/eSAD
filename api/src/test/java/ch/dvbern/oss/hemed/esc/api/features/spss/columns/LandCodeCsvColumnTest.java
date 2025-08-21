package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LandCodeCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final LandCodeCsvColumn column = new LandCodeCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungKindDaten untersuchungKindDaten = UntersuchungKindDatenBuilder.defaultUntersuchungDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);

        // empty lancode -> null
        assertThat(column.extract(einladung)).isEmpty();

        // with lancode
        untersuchungKindDaten.setNationalitaet(Land.SCHWEIZ);
        assertThat(column.extract(einladung)).isEqualTo("1");

        // with lancode
        untersuchungKindDaten.setNationalitaet(Land.BOLIVIEN);
        assertThat(column.extract(einladung)).isEqualTo("71");
    }
}
