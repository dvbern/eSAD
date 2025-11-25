package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KlassenbezeichnungCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final KlassenbezeichnungCsvColumn column = new KlassenbezeichnungCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungKindDaten -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Klasse -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        // With Klasse
        untersuchungKindDaten.setKlasse("Klasse 4a");
        assertThat(column.extract(einladung)).isEqualTo("Klasse 4a");
    }
}
