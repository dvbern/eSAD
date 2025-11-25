package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchulhausnameCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final SchulhausnameCsvColumn column = new SchulhausnameCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungKindDaten untersuchungKindDaten = UntersuchungKindDatenBuilder.defaultUntersuchungDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);

        // Without Schule -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // With Schule
        untersuchungKindDaten.setSchulhaus("Allgemeine Gewerbeschule Basel");
        assertThat(column.extract(einladung)).isEqualTo("Allgemeine Gewerbeschule Basel");
    }
}
