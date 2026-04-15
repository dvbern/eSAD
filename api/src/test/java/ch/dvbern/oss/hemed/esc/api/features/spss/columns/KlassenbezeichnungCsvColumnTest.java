package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KlassenbezeichnungCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final KlassenbezeichnungCsvColumn column = new KlassenbezeichnungCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        final Klasse klasse = new Klasse();
        klasse.setBezeichnung("Test 9a");
        dossier.getKind().setKlasse(klasse);
        einladung.setDossier(dossier);

        assertThat(column.extract(einladung)).isEqualTo("Test 9a");

        // without UntersuchungKindDaten -> Klasse from Dossier
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEqualTo("Test 9a");

        // without Klasse -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        // With Klasse
        untersuchungKindDaten.setKlasse("Klasse 4a");
        assertThat(column.extract(einladung)).isEqualTo("Klasse 4a");
    }
}
