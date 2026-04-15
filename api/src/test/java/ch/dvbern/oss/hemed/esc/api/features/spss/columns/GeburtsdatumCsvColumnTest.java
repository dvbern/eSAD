package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungKindDatenBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeburtsdatumCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final GeburtsdatumCsvColumn column = new GeburtsdatumCsvColumn(configForCsv);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        dossier.getKind().setGeburtsdatum(LocalDate.of(2009, 10, 10));
        einladung.setDossier(dossier);

        assertThat(column.extract(einladung)).isEqualTo("10.10.2009");

        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);

        UntersuchungKindDaten untersuchungKindDaten = UntersuchungKindDatenBuilder.defaultUntersuchungDaten();
        untersuchungKindDaten.setGeburtsdatum(LocalDate.of(2010, 10, 4));
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);

        assertThat(column.extract(einladung)).isEqualTo("04.10.2010");
    }
}
