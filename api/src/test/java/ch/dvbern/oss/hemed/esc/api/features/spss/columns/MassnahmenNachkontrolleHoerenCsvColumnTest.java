package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MassnahmenNachkontrolleHoerenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenNachkontrolleHoerenCsvColumn column =
            new MassnahmenNachkontrolleHoerenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        AnlassFlags anlassFlags = new AnlassFlags();
        untersuchungMassnahmen.setNachkontrolle(anlassFlags);

        assertThat(column.extract(einladung)).isEmpty();

        anlassFlags.setNachkontrolleHoeren(true);

        assertThat(column.extract(einladung)).isEqualTo("1");
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final MassnahmenNachkontrolleHoerenCsvColumn column =
            new MassnahmenNachkontrolleHoerenCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        AnlassFlags anlassFlags = new AnlassFlags();
        untersuchungMassnahmen.setNachkontrolle(anlassFlags);

        assertThat(column.extract(einladung)).isEmpty();

        anlassFlags.setNachkontrolleHoeren(true);

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        assertThat(column.extract(einladung)).isEqualTo("Ja");
    }

}
