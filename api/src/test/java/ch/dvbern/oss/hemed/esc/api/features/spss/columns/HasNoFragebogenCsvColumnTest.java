package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HasNoFragebogenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final HasNoFragebogenCsvColumn column = new HasNoFragebogenCsvColumn(config, null);
        Einladung einladung = new Einladung();

        // Fragebogen is null
        assertThat(column.extract(einladung)).isEqualTo("1");

        // Fragebogen is not null
        Notiz fragebogenNotiz = new Notiz();
        fragebogenNotiz.setTyp(NotizTyp.FRAGEBOGEN);
        einladung.setNotizen(List.of(fragebogenNotiz));
        assertThat(column.extract(einladung)).isEmpty();
    }
}
