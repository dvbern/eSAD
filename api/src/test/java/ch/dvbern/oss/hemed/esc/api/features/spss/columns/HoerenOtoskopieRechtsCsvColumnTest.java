package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class HoerenOtoskopieRechtsCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(OtoskopieResultat.class)
    void extract(OtoskopieResultat otoskopieResultat) {
        // ARRANGE
        final HoerenOtoskopieRechtsCsvColumn column =
            new HoerenOtoskopieRechtsCsvColumn(config, null, otoskopieResultat);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungHoerenOtoskopie hoerenOtoskopie = new UntersuchungHoerenOtoskopie();
        untersuchung.setUntersuchungHoerenOtoskopie(hoerenOtoskopie);

        assertThat(column.extract(einladung)).isEmpty();

        hoerenOtoskopie.setOtoskopieRechts(List.of(OtoskopieResultat.values()));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
