package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class FingeroppositionCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(Fingeropposition.class)
    void extract(Fingeropposition fingeropposition) {
        // ARRANGE
        final FingeroppositionCsvColumn column = new FingeroppositionCsvColumn(config, null, fingeropposition);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArzt untersuchungArzt = new UntersuchungArzt();
        untersuchung.setUntersuchungArzt(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setFingeropposition(List.of(Fingeropposition.values()));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
