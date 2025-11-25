package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KrankheitImpfungVerabreichtCsvColumnTest extends AbstractBaseColumnTest {

    @ParameterizedTest
    @EnumSource(Krankheit.class)
    void extractor(Krankheit krankheit) {
        // ARRANGE
        final KrankheitImpfungVerabreichtCsvColumn column =
            new KrankheitImpfungVerabreichtCsvColumn(config, null, krankheit);
        final Einladung einladung = new Einladung();
        einladung.setAnlass(new Anlass());

        // without Untersuchung -> empty string
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfungen -> empty string
        Untersuchung untersuchung = new Untersuchung();
        einladung.setUntersuchung(untersuchung);
        untersuchung.setEinladung(einladung);
        assertThat(column.extract(einladung)).isEmpty();

        // without UntersuchungImpfstatusKrankheit
        UntersuchungImpfungen untersuchungImpfungen = new UntersuchungImpfungen();
        untersuchung.setUntersuchungImpfungen(untersuchungImpfungen);
        untersuchungImpfungen.setUntersuchung(untersuchung);
        assertThat(column.extract(einladung)).isEmpty();

        UntersuchungImpfungIntern untersuchungImpfungIntern =
            UntersuchungImpfungInternBuilder.create(builder
                -> builder.forUntersuchungImpfungen(untersuchungImpfungen)
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT));
        when(untersuchungImpfungInternServiceMock.findByUntersuchung(any()))
            .thenReturn(List.of(untersuchungImpfungIntern));

        assertThat(column.extract(einladung)).isEmpty();

        Set<UntersuchungImpfungKrankheitImmunitaet> krankheitImmunitaetsList = new HashSet<>();
        krankheitImmunitaetsList.add(new UntersuchungImpfungKrankheitImmunitaet(
            untersuchungImpfungIntern,
            krankheit,
            false));
        untersuchungImpfungIntern.setKrankheitenImmunitaeten(krankheitImmunitaetsList);

        assertThat(column.extract(einladung)).isEqualTo("1");
    }
}
