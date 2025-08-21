package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LastUpdateImpfungenCsvColumnTest extends AbstractBaseColumnTest {

    @Test
    void extractor() {
        // ARRANGE
        final LastUpdateImpfungenCsvColumn column = new LastUpdateImpfungenCsvColumn(config, null);
        final Einladung einladung = new Einladung();

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        // without ImpfstatusKrankheit -> empty string
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchungImpfungen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        assertThat(column.extract(einladung)).isEmpty();

        // with empty list -> empty string
        when(untersuchungImpfstatusKrankheitServiceMock.findByUntersuchung(any())).thenReturn(List.of());
        assertThat(column.extract(einladung)).isEmpty();

        // with list -> find most recent date
        when(untersuchungImpfstatusKrankheitServiceMock.findByUntersuchung(any())).thenReturn(List.of(
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.PERTUSSIS)
                .withTimestampMutiert(OffsetDateTime.of(2021, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))),
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.MUMPS)
                .withTimestampMutiert(OffsetDateTime.of(2022, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))),
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.VARIZELLEN)
                .withTimestampMutiert(OffsetDateTime.of(2022, 1, 1, 12, 0, 1, 0, ZoneOffset.UTC)))
        ));
        assertThat(column.extract(einladung)).isEqualTo("01.01.2022");
    }
}
