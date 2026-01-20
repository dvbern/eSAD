package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuartierCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final QuartierCsvColumn column = new QuartierCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Kind kind = KindBuilder.defaultKind();
        einladung.setDossier(Dossier.forKind(kind));

        // Without Schule -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // Without Quartier -> empty string
        Schule schule = new Schule();
        kind.setSchule(schule);
        assertThat(column.extract(einladung)).isEmpty();

        // With Quartier
        Quartier quartier = new Quartier();
        schule.setQuartier(quartier);

        // GBW
        quartier.setQuartierName("GBW");
        assertThat(column.extract(einladung)).isEqualTo("10");

        // GBO
        quartier.setQuartierName("GBO");
        assertThat(column.extract(einladung)).isEqualTo("11");

        // KB
        quartier.setQuartierName("KB");
        assertThat(column.extract(einladung)).isEqualTo("12");

        // LAND
        quartier.setQuartierName("LAND");
        assertThat(column.extract(einladung)).isEqualTo("13");

        // Anything else
        quartier.setQuartierName("Blub");
        assertThat(column.extract(einladung)).isEmpty();
    }

    @Test
    void extractCsv() {
        // ARRANGE
        final QuartierCsvColumn column = new QuartierCsvColumn(configForCsv, null);
        final Einladung einladung = new Einladung();

        Kind kind = KindBuilder.defaultKind();
        einladung.setDossier(Dossier.forKind(kind));

        // Without Schule -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // Without Quartier -> empty string
        Schule schule = new Schule();
        kind.setSchule(schule);
        assertThat(column.extract(einladung)).isEmpty();

        // With Quartier
        Quartier quartier = new Quartier();
        schule.setQuartier(quartier);

        // GBW
        quartier.setQuartierName("GBW");
        assertThat(column.extract(einladung)).isEqualTo("GBW");
    }
}
