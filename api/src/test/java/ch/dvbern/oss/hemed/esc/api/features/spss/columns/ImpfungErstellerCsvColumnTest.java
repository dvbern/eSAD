package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfungErstellerCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungErstellerCsvColumn column = new ImpfungErstellerCsvColumn(configForCsv);

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now());

        // Impfung ohne Erfasser
        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("-");

        // Erfasser setzen
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withVorname("Hans").withNachname("Meier"));
        untersuchungImpfungIntern.setUserErfasst(benutzer);

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Meier Hans");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungErstellerCsvColumn column = new ImpfungErstellerCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
