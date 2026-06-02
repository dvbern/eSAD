package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchulkreisCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SchulkreisCsvColumn column = new SchulkreisCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        einladung.setDossier(dossier);

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungKindDaten -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Schulkreis -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        // With Schulkreis
        untersuchungKindDaten.setSchulkreis("Allgemeine Gewerbeschule Basel");
        assertThat(column.extract(einladung)).isEqualTo("Allgemeine Gewerbeschule Basel");
    }
}
