package ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationExceptionAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SozialversicherungsNummerTest {

    @Test
    void testPruefziffer0() {
        SozialversicherungsNummer sn = new SozialversicherungsNummer(7562844768650L);
        assertThat(sn.isValid()).isTrue();
    }

    @Test
    void testValidFromString() {
        SozialversicherungsNummer sn = new SozialversicherungsNummer("7569217076985");
        assertThat(sn.isValid()).isTrue();
        SozialversicherungsNummer sn2 = new SozialversicherungsNummer("756.9217.0769.85");
        assertThat(sn).isEqualTo(sn2);
        assertThat(sn2.isValid()).isTrue();
    }

    @Test
    void testValidFromLong() {
        SozialversicherungsNummer sn = new SozialversicherungsNummer(7569217076985L);
        assertThat(sn.isValid()).isTrue();
    }

    @Test
    void testInvalid() {
        AppValidationException ex = assertThrows(
            AppValidationException.class,
            () -> new SozialversicherungsNummer(7569227076983L)
        );
        AppValidationExceptionAssert.assertThatAppValidationException(ex)
            .hasClientKey("InvalidSozialversicherungsnummer")
            .hasRawMessageContaining("AppValidation.INVALID_SOZIALVERSICHERUNGSNUMMER");
    }

    @Test
    void testGetNumberString() {
        String nr = "7569217076985";
        SozialversicherungsNummer nummer = new SozialversicherungsNummer(nr);
        assertThat(nr).isEqualTo(nummer.toString());
    }

    @Test
    void testTooShort() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new SozialversicherungsNummer("1")
        );
        assertThat(ex)
            .hasMessageContaining(" 1 ");
    }

    @Test
    void testTooLong() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new SozialversicherungsNummer("12345678901234")
        );
        assertThat(ex)
            .hasMessageContaining(" 12345678901234 ");
    }

    @Test
    void testToString() {
        assertThat(new SozialversicherungsNummer("7569217076985").toFormattedString()).isEqualTo("756.9217.0769.85");
    }

    @Test
    void testPruefziffer() {
        assertThat(new SozialversicherungsNummer(7561234567897L).getPruefziffer()).isEqualTo(7);
        assertThat(new SozialversicherungsNummer(7562229390322L).getPruefziffer()).isEqualTo(2);
        assertThat(new SozialversicherungsNummer(7569217076985L).getPruefziffer()).isEqualTo(5);
        assertThat(new SozialversicherungsNummer(7562844768650L).getPruefziffer()).isZero();
        assertThat(new SozialversicherungsNummer(7561277671407L).getPruefziffer()).isEqualTo(7);
        assertThat(new SozialversicherungsNummer(7567779844851L).getPruefziffer()).isEqualTo(1);
    }

    @Test
    void testSvnrGeneration() {
        for (int i = 0; i < 5; i++) {
            SozialversicherungsNummer sn = SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer();
            assertThat(sn.isValid()).isTrue();
        }
    }
}
