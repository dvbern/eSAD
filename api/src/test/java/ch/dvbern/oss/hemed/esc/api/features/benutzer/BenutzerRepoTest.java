package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
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

    private static final int LIMIT = 200;

    private final BenutzerRepo benutzerRepo;
    private final OrganisationService organisationService;

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
        Organisation organisation = organisationService.getZentraleOrganisation();
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_recently_logged_in")
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzerLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_long_since_logged_in")
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withRandomNachname("Ben_never_logged_in")
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzerLongSinceLoggedIn);
        benutzerRepo.persist(benutzerNeverLoggedIn);

        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, LIMIT);
        // ASSERT
        Assertions.assertThat(benutzerToDeactivate)
            .hasSizeLessThan(LIMIT)
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
                .withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Organisation organisation = organisationService.getZentraleOrganisation();
        Benutzer mpaLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        benutzerRepo.persist(systemAdminLongSinceLoggedIn);
        benutzerRepo.persist(mpaLongSinceLoggedIn);
        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, LIMIT);
        // ASSERT
        Assertions.assertThat(benutzerToDeactivate)
            .hasSizeLessThan(LIMIT)
            .contains(mpaLongSinceLoggedIn.getId())
            .doesNotContain(systemAdminLongSinceLoggedIn.getId());
    }

    @Test
    @TestAsAdminBS
    void deactivateListOfBenutzer() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00"));
        Organisation organisation = organisationService.getZentraleOrganisation();
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzerLongSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzerLongSinceLoggedIn);
        benutzerRepo.persist(benutzerNeverLoggedIn);
        // ACT
        List<ID<Benutzer>> benutzerToDeactivate = benutzerRepo.getBenutzerToDeactivate(60, LIMIT);
        Assertions.assertThat(benutzerToDeactivate)
            .hasSizeLessThan(LIMIT) // Wir lesen nur die ersten 100
            .contains(
                benutzerLongSinceLoggedIn.getId(),
                benutzerNeverLoggedIn.getId())
            .doesNotContain(
                benutzerRecentlyLoggedIn.getId());
        long anzahlChanged = benutzerRepo.deactivateListOfBenutzer(benutzerToDeactivate);
        // ASSERT
        Assertions.assertThat(anzahlChanged).isGreaterThan(2); // Eventuell weiter von anderen Tests vorhanden
        Benutzer benutzerRecentlyLoggedInAfter = benutzerRepo.findById(benutzerRecentlyLoggedIn.getId()).orElseThrow();
        Benutzer benutzerLongSinceLoggedInAfter = benutzerRepo.findById(benutzerLongSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzerNeverLoggedInAfter = benutzerRepo.findById(benutzerNeverLoggedIn.getId()).orElseThrow();
        Assertions.assertThat(benutzerRecentlyLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        Assertions.assertThat(benutzerLongSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        Assertions.assertThat(benutzerNeverLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
    }
}
