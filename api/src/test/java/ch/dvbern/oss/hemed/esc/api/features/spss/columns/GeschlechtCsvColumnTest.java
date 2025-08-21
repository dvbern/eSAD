package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeschlechtCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final GeschlechtCsvColumn column = new GeschlechtCsvColumn(config, null);
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungKindDaten untersuchungKindDaten = UntersuchungKindDatenBuilder.defaultUntersuchungDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);

        // Männlich
        untersuchungKindDaten.setGeschlecht(Geschlecht.MAENNLICH);
        assertThat(column.extract(einladung)).isEqualTo("1");

        // Weiblich
        untersuchungKindDaten.setGeschlecht(Geschlecht.WEIBLICH);
        assertThat(column.extract(einladung)).isEqualTo("2");

        // Unbekannt
        untersuchungKindDaten.setGeschlecht(Geschlecht.UNBEKANNT);
        assertThat(column.extract(einladung)).isEqualTo("3");

        // Divers
        untersuchungKindDaten.setGeschlecht(Geschlecht.DIVERS);
        assertThat(column.extract(einladung)).isEqualTo("4");
    }
}
