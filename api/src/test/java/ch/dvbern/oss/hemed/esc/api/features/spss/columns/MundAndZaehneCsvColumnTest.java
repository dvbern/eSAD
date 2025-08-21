package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class MundAndZaehneCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(MundAndZaehne.class)
    void extract(MundAndZaehne mundAndZaehne) {
        // ARRANGE
        final MundAndZaehneCsvColumn column = new MundAndZaehneCsvColumn(config, null, mundAndZaehne);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungArzt untersuchungArzt = new UntersuchungArzt();
        untersuchung.setUntersuchungArzt(untersuchungArzt);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungArzt.setMundAndZaehne(List.of(MundAndZaehne.values()));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
