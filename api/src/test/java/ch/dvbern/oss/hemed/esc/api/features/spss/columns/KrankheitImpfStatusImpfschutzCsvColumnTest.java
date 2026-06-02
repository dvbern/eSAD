package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KrankheitImpfStatusImpfschutzCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        Krankheit krankheit = Krankheit.DIPHTERIE;
        final KrankheitImpfStatusImpfschutzCsvColumn column =
            new KrankheitImpfStatusImpfschutzCsvColumn(configForCsv, null, krankheit);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfstatusKrankheit
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfstatusKrankheit untersuchungImpfstatusKrankheit =
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(krankheit));
        when(untersuchungImpfstatusKrankheitServiceMock.findByUntersuchungAndKrankheit(any(), any())).thenReturn(
            Optional.of(untersuchungImpfstatusKrankheit));

        assertThat(column.extract(einladung)).isEmpty();

        when(tlMock.translate("printer.boolean.true")).thenReturn("Ja");
        untersuchungImpfstatusKrankheit.setImpfschutzGegeben(true);
        assertThat(column.extract(einladung)).isEqualTo("Ja");

    }
}
