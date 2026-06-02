package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MassnahmenBereitsBehandeltSelectedCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenBereitsBehandeltSelectedCsvColumn column =
            new MassnahmenBereitsBehandeltSelectedCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        untersuchungMassnahmen.setBereitsBehandeltSehen(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        untersuchungMassnahmen.setBereitsBehandeltHoeren(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        untersuchungMassnahmen.setBereitsBehandeltGroesseGewicht(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        untersuchungMassnahmen.setBereitsBehandeltSprache(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        untersuchungMassnahmen.setBereitsBehandeltMotorik(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        untersuchungMassnahmen.setBereitsBehandeltAnderes(true);
        assertThat(column.extract(einladung)).isEqualTo("1");

        setToFalseBereitsBehandelt(untersuchungMassnahmen);
        assertThat(column.extract(einladung)).isEmpty();
    }

    private void setToFalseBereitsBehandelt(UntersuchungMassnahmen untersuchungMassnahmen) {
        untersuchungMassnahmen.setBereitsBehandeltSehen(false);
        untersuchungMassnahmen.setBereitsBehandeltHoeren(false);
        untersuchungMassnahmen.setBereitsBehandeltGroesseGewicht(false);
        untersuchungMassnahmen.setBereitsBehandeltSprache(false);
        untersuchungMassnahmen.setBereitsBehandeltMotorik(false);
        untersuchungMassnahmen.setBereitsBehandeltAnderes(false);
    }
}
