package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.LocalDate;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KrankheitJahrDerLetztenImpfungCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(Krankheit.class)
    void extractor(Krankheit krankheit) {
        // ARRANGE
        final KrankheitJahrDerLetztenImpfungCsvColumn column =
            new KrankheitJahrDerLetztenImpfungCsvColumn(config, null, krankheit);
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

        assertThat(column.extract(einladung)).isEqualTo("55");

        untersuchungImpfstatusKrankheit.setDatumLetzteImpfung(LocalDate.of(2015, 1, 1));
        assertThat(column.extract(einladung)).isEqualTo("2015");
    }
}
