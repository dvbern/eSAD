package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.Visum;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KrankheitImpfungDoppelkontrolleVisum1CsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        final KrankheitImpfungDoppelkontrolleVisum1CsvColumn column =
            new KrankheitImpfungDoppelkontrolleVisum1CsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfstatusKrankheit
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();


        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        Benutzer benutzer = new Benutzer();
        benutzer.setVorname("Simon");
        benutzer.setNachname("Maurer");
        Visum visum = new Visum();
        visum.setPerson(benutzer);
        untersuchungImpfungen.setVisum1(visum);

        assertThat(column.extract(einladung)).isEqualTo("Maurer Simon");
    }
}
