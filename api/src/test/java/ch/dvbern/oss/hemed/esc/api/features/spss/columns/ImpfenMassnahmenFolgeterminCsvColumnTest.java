package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ImpfenMassnahmenFolgeterminCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfenMassnahmenFolgeterminCsvColumn column = new ImpfenMassnahmenFolgeterminCsvColumn(configForCsv);
        final Einladung einladung = new Einladung();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen = new UntersuchungImpfungenMassnahmen();
        untersuchungImpfungen.setUntersuchungImpfungenMassnahmen(untersuchungImpfungenMassnahmen);
        assertThat(column.extract(einladung)).isEmpty();

        // ImpfausweisUnvollstaendig -> is not checked
        when(tlMock.translate("printer.boolean.false")).thenReturn("Nein");
        untersuchungImpfungenMassnahmen.setVormerkenFuerGruppenFolgetermin(false);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEqualTo("Nein");

        // ImpfausweisNichtVorhanden -> is checked
        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");

        untersuchungImpfungenMassnahmen.setVormerkenFuerGruppenFolgetermin(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }
}
