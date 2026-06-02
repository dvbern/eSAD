package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchulhausnummerCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SchulhausnummerCsvColumn column = new SchulhausnummerCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        einladung.setDossier(dossier);

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungKindDaten -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Schulhausnummer -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        // With Schulhausnummer
        untersuchungKindDaten.setSchulhausnummer("456");
        assertThat(column.extract(einladung)).isEqualTo("456");
    }
}
