package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdNrCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final IdNrCsvColumn column = new IdNrCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        Kind kind = KindBuilder.defaultKind();
        einladung.setDossier(Dossier.forKind(kind));

        // esadId null -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // esadId not null
        kind.setEsadId("12 34 56 78 9");
        assertThat(column.extract(einladung)).isEqualTo("123456789");
    }
}
