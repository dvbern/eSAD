package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchultypCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        final SchulartCsvColumn column = new SchulartCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        einladung.setDossier(dossier);

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungKindDaten -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Klassenstufe -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        untersuchungKindDaten.setKlasseId(UUID.randomUUID().toString());
        untersuchungKindDaten.setKlassentyp("KG");
        untersuchungKindDaten.setKlassenstufe(Klassenstufe.KINDERGARTEN_1);

        assertThat(column.extract(einladung)).isEqualTo("KG");
    }
}
