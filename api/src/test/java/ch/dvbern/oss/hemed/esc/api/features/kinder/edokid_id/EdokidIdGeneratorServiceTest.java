package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EdokidIdGeneratorServiceTest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);
    private final EdokidIdGeneratorService edokidIdGeneratorService = new EdokidIdGeneratorService(
        appClock,
        Mockito.mock(EdokidIdRepo.class)
    );

    @BeforeEach
    void setUp() {
        clockProvider.resetUsing(OffsetDateTime.now().toInstant());
    }

    private String getSchuljahrFormatted() {
        LocalDate localDate = this.appClock.localDateTime().now().toLocalDate();

        int jahr = localDate.isBefore(LocalDate.of(localDate.getYear(), Month.AUGUST, 1)) ?
            localDate.getYear() - 1 :
            localDate.getYear();

        return String.valueOf(Integer.parseInt(Integer.toString(jahr).substring(2)));
    }

    @Test
    void test_EdokidIdTestGenerator() {
        // ARRANGE
        String year = getSchuljahrFormatted();

        // ACT
        EdokidId id1 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id2 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id3 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id4 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id5 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id6 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id7 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id8 = EdokidIdTestGenerator.getNextEdokidId(null);
        EdokidId id9 = EdokidIdTestGenerator.getNextEdokidId(null);

        // ASSERT
        assertThat(id1.toFormattedString()).isEqualTo("A0 01 000 " + year);
        assertThat(id2.toFormattedString()).isEqualTo("A0 02 000 " + year);
        assertThat(id3.toFormattedString()).isEqualTo("A0 03 000 " + year);
        assertThat(id4.toFormattedString()).isEqualTo("A0 04 000 " + year);
        assertThat(id5.toFormattedString()).isEqualTo("A0 05 000 " + year);
        assertThat(id6.toFormattedString()).isEqualTo("A0 06 000 " + year);
        assertThat(id7.toFormattedString()).isEqualTo("A0 07 000 " + year);
        assertThat(id8.toFormattedString()).isEqualTo("A0 08 000 " + year);
        assertThat(id9.toFormattedString()).isEqualTo("A0 09 000 " + year);
    }

    @Test
    void numerWith4Digits_shouldThrowException() {
        // ACT & ASSERT
        Schule schule = SchuleBuilder.defaultSchule();
        schule.setSchulhausnummer("1234");
        String formattedSchulhausnummer = schule.getFormattedSchulhausnummer();
        assertThatThrownBy(() -> new EdokidId('A', "1", formattedSchulhausnummer, 24)
        ).isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.INVALID_NUMMER");
    }

    @Test
    void getCorrectEintrittsjahr() {
        // ARRANGE
        OffsetDateTime offsetDateTime = ZonedDateTime.of(
                LocalDateTime.of(2025, 7, 31, 12, 0),
                DateConst.ZONE_ID)
            .toOffsetDateTime();
        this.clockProvider.resetUsing(offsetDateTime.toInstant());
        // ACT
        int actualEintrittsjahr = edokidIdGeneratorService.getEintrittsJahr();

        // ASSERT
        assertThat(actualEintrittsjahr).isEqualTo(24);
        // ARRANGE
        clockProvider.resetUsing(offsetDateTime.plusHours(12).toInstant());
        // ACT
        int nextEintrittsjahr = edokidIdGeneratorService.getEintrittsJahr();
        assertThat(nextEintrittsjahr).isEqualTo(25);
    }

    @Test
    void getSchulhausNummer_ifSchuleNotSet() {
        // ARRANGE
        String expectedSchulhausNummer = "000";

        // ACT
        String actualSchulhausNummer = edokidIdGeneratorService.getSchulhausNummer(null);

        // ASSERT
        assertThat(actualSchulhausNummer).isNotNull().isEqualTo(expectedSchulhausNummer);
    }

    @Test
    void getSchulhausNummer_ifSchulhausNummerNotSet_shouldThrowException() {
        // ARRANGE
        Schule schule = SchuleBuilder.defaultSchule();
        schule.setSchulhausnummer(null);

        // ACT & ASSERT
        assertThatThrownBy(() -> edokidIdGeneratorService.getSchulhausNummer(schule)
        ).isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.NO_SCHULHAUSNUMMER, args: schulhaus=" + schule.getSchulhaus());
    }

    @Test
    void getSchulhausNummer() {
        // ARRANGE
        Schule schule = SchuleBuilder.defaultSchule();
        String expectedSchulhausNummer = "123";

        // ACT
        String actualSchulhausNummer = edokidIdGeneratorService.getSchulhausNummer(schule);

        // ASSERT
        assertThat(actualSchulhausNummer).isNotNull().isEqualTo(expectedSchulhausNummer);
    }

    @Test
    void getSchulhausNummerFormattedIfSchulhausnummerFormatIsWrong() {
        // ARRANGE
        Schule schule = SchuleBuilder.defaultSchule();
        schule.setSchulhausnummer("1234");

        // ACT
        String actualSchulhausNummer = edokidIdGeneratorService.getSchulhausNummer(schule);

        // ASSERT
        assertThat(actualSchulhausNummer).isNotNull().isEqualTo("1234");
    }

    @ParameterizedTest
    @CsvSource({
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
    })
    void getNext5Laufnummern(int einTrittsJahr) {
        var edokidIdSeq = new EdokidIdSequence(einTrittsJahr, 'A', 0);
        // ACT
        edokidIdSeq.increment();
        String seq1 = edokidIdSeq.getCharacter() + edokidIdSeq.getFormattedLaufnummer();
        edokidIdSeq.increment();
        String seq2 = edokidIdSeq.getCharacter() + edokidIdSeq.getFormattedLaufnummer();
        edokidIdSeq.increment();
        String seq3 = edokidIdSeq.getCharacter() + edokidIdSeq.getFormattedLaufnummer();
        edokidIdSeq.increment();
        String seq4 = edokidIdSeq.getCharacter() + edokidIdSeq.getFormattedLaufnummer();
        edokidIdSeq.increment();
        String seq5 = edokidIdSeq.getCharacter() + edokidIdSeq.getFormattedLaufnummer();

        // ASSERT
        assertThat(seq1).isEqualTo("A001");
        assertThat(seq2).isEqualTo("A002");
        assertThat(seq3).isEqualTo("A003");
        assertThat(seq4).isEqualTo("A004");
        assertThat(seq5).isEqualTo("A005");
    }

}
