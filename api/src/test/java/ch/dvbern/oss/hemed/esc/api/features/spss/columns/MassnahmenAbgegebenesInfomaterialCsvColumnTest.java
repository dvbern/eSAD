package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MassnahmenAbgegebenesInfomaterialCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final MassnahmenAbgegebenesInfomaterialCsvColumn column =
            new MassnahmenAbgegebenesInfomaterialCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        assertThat(column.extract(einladung)).isEmpty();

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungMassnahmen untersuchungMassnahmen = new UntersuchungMassnahmen();
        untersuchung.setUntersuchungMassnahmen(untersuchungMassnahmen);

        assertThat(column.extract(einladung)).isEmpty();

        Infomaterial infomaterial = InfomaterialBuilder.create(
            builder -> builder
                .withRandomName("InfomaterialForTest"));

        untersuchungMassnahmen.setAbgegebenesInfomaterial(Set.of(infomaterial));

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
