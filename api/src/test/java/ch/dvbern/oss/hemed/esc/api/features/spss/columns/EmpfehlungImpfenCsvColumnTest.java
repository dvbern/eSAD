package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EmpfehlungImpfenCsvColumnTest extends AbstractBaseColumnTest {
    @Test
    void extract() {
        // ARRANGE
        final EmpfehlungImpfenCsvColumn column =
            new EmpfehlungImpfenCsvColumn(configForCsv, ImpfungenMassnahmenEmpfehlung.IMPFUNG_BEIM_HAUSARZT);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        UntersuchungImpfungenMassnahmen impfenMassnahmen = new UntersuchungImpfungenMassnahmen();
        impfungen.setUntersuchungImpfungenMassnahmen(impfenMassnahmen);
        untersuchung.setUntersuchungImpfungen(impfungen);

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        impfenMassnahmen.setEmpfehlung(EnumSet.of(ImpfungenMassnahmenEmpfehlung.IMPFUNG_BEIM_HAUSARZT));
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
