package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpfungVerabreicherCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final ImpfungVerabreicherCsvColumn column = new ImpfungVerabreicherCsvColumn(configForCsv);

        // Impfung Intern
        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now());

        // Impfung ohne Verabreicher
        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("-");

        // Verabreicher setzen
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withVorname("Hans").withNachname("Meier"));
        untersuchungImpfungIntern.setUserDurchgefuehrt(benutzer);

        assertThat(column.extract(untersuchungImpfungIntern)).isEqualTo("Meier Hans");
    }

    @Test
    void extractFromEinladung() {
        // ARRANGE
        final ImpfungVerabreicherCsvColumn column = new ImpfungVerabreicherCsvColumn(configForCsv);

        // Einladung
        Einladung einladung = new Einladung();
        assertThat(column.extract(einladung)).isEmpty();
    }
}
