package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlterCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final AlterCsvColumn column = new AlterCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Kind kind = KindBuilder.defaultKind();
        kind.setGeburtsdatum(LocalDate.of(2010, 10, 4));
        Dossier dossier = Dossier.forKind(kind);

        Anlass anlass = new Anlass();
        anlass.setDatum(LocalDate.of(2024, 11, 17));

        einladung.setDossier(dossier);
        einladung.setAnlass(anlass);

        assertThat(column.extract(einladung)).isEqualTo("14 ¹/₁₂");
    }
}
