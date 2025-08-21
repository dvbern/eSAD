package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.Collections;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerFixtureBuilder;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenutzerTest {

    @Test
    void fach_rollen_can_be_empty() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();
        benutzer.setFachRollen(Collections.emptyList());
        assertTrue(benutzer.getFachRollen().isEmpty());

    }

    @Test
    void fach_rollen_should_be_read_and_writable() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        benutzer.setFachRollen(Collections.singletonList(FachRolle.ADMIN));
        assertEquals(1, benutzer.getFachRollen().size());
        assertEquals(FachRolle.ADMIN, benutzer.getFachRollen().getFirst());
    }

    @Test
    void fach_rollen_are_returned_in_ordinal_order() {
        Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer().build();

        benutzer.setFachRollen(List.of(
            FachRolle.SCHULAERZTIN,
            FachRolle.MPA,
            FachRolle.ADMIN,
            FachRolle.ORTHOPISTIN,
            FachRolle.EMPFANG));

        Assertions.assertThat(benutzer.getFachRollen())
            .containsExactly(
                FachRolle.EMPFANG,
                FachRolle.MPA,
                FachRolle.ORTHOPISTIN,
                FachRolle.SCHULAERZTIN,
                FachRolle.ADMIN);
    }
}
