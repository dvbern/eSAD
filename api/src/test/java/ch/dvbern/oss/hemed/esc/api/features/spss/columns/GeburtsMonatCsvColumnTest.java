package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeburtsMonatCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final GeburtsMonatCsvColumn column = new GeburtsMonatCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        dossier.getKind().setGeburtsdatum(LocalDate.of(2009, 4, 10));
        einladung.setDossier(dossier);

        assertThat(column.extract(einladung)).isEqualTo("4");

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEqualTo("4");

        UntersuchungKindDaten untersuchungKindDaten = UntersuchungKindDatenBuilder.defaultUntersuchungDaten();
        untersuchungKindDaten.setGeburtsdatum(LocalDate.of(2011, 10, 13));
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);

        assertThat(column.extract(einladung)).isEqualTo("10");
    }
}
