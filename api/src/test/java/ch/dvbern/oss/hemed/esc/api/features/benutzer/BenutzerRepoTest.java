package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class BenutzerRepoTest extends AbstractITest {

    private final BenutzerRepo benutzerRepo;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getBenutzerToDeactivate() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00"));
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_recently_logged_in")
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzerLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_long_since_logged_in")
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_never_logged_in")
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzerLongSinceLoggedIn);
        benutzerRepo.persist(benutzerNeverLoggedIn);

        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, 100);
        // ASSERT
        Assertions.assertThat(benutzerToDeactivate)
            .contains(
                benutzerLongSinceLoggedIn.getId(),
                benutzerNeverLoggedIn.getId())
            .doesNotContain(
                benutzerRecentlyLoggedIn.getId());
    }

    @Test
    @TestAsAdminBS
    void getBenutzerToDeactivate_does_not_return_SYSTEM_ADMIN() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00"));
        Benutzer systemAdminLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.SYSTEM_ADMIN))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Benutzer mpaLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        benutzerRepo.persist(systemAdminLongSinceLoggedIn);
        benutzerRepo.persist(mpaLongSinceLoggedIn);
        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, 100);
        // ASSERT
        Assertions.assertThat(benutzerToDeactivate)
            .contains(mpaLongSinceLoggedIn.getId())
            .doesNotContain(systemAdminLongSinceLoggedIn.getId());
    }

    @Test
    @TestAsAdminBS
    void deactivateListOfBenutzer() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00"));
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzerLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzerLongSinceLoggedIn);
        benutzerRepo.persist(benutzerNeverLoggedIn);
        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, 100);
        long anzahlChanged = benutzerRepo.deactivateListOfBenutzer(benutzerToDeactivate);
        // ASSERT
        Assertions.assertThat(anzahlChanged).isGreaterThan(2); // Eventuell weiter von anderen Tests vorhanden
        Benutzer benutzerRecentlyLoggedInAfter = benutzerRepo.findById(benutzerRecentlyLoggedIn.getId()).orElseThrow();
        Benutzer benutzerLongSinceLoggedInAfter = benutzerRepo.findById(benutzerLongSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzerNeverLoggedInAfter = benutzerRepo.findById(benutzerNeverLoggedIn.getId()).orElseThrow();
        Assertions.assertThat(benutzerRecentlyLoggedInAfter.isAktiv()).isTrue();
        Assertions.assertThat(benutzerLongSinceLoggedInAfter.isAktiv()).isFalse();
        Assertions.assertThat(benutzerNeverLoggedInAfter.isAktiv()).isFalse();
    }
}
