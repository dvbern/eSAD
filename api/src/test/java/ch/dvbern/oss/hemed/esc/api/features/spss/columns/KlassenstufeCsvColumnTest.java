package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class KlassenstufeCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extract() {
        // ARRANGE
        final KlassenstufeCsvColumn column = new KlassenstufeCsvColumn(config, null);
        final Einladung einladung = new Einladung();
        final Dossier dossier = DossierBuilder.defaultDossier();
        einladung.setDossier(dossier);

        // without Untersuchung -> Daten von Kind
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungKindDaten -> Daten von Kind
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without Klassenstufe -> empty string
        UntersuchungKindDaten untersuchungKindDaten = new UntersuchungKindDaten();
        untersuchung.setUntersuchungKindDaten(untersuchungKindDaten);
        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translateValue(Klassenstufe.PRIMAR_KLASSE_4)).thenReturn("Primar 4");
        // With Klassenstufe
        untersuchungKindDaten.setKlassenstufe(Klassenstufe.PRIMAR_KLASSE_4);
        assertThat(column.extract(einladung)).isEqualTo("Primar 4");
    }
}
