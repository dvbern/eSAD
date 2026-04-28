package ch.dvbern.oss.hemed.esc.api.shared.abstractentity;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerFixtureBuilder;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AbstractEntityListenerTest {

    private final Benutzer benutzerOnPersist = BenutzerFixtureBuilder.validBenutzer()
        .build();
    private final Benutzer benutzerOnChange = BenutzerFixtureBuilder.validBenutzer(
            ID.parse("55555555-5555-5555-5555-555555555555", Benutzer.class),
            EmailAddress.fromString("changer@somewhere.org")
        )
        .build();

    private Benutzer currentUser;

    // <editor-fold desc="currentUserStub returning currentUser" defaultstate="collapsed">
    private final CurrentUserService currentUserStub = new CurrentUserService() {
        @Override
        public ID<Benutzer> currentUserID() {
            return currentUser.getId();
        }

        @Override
        public EmailAddress currentUserName() {
            return currentUser.getEmail();
        }

        @Override
        public Optional<Benutzer> findCurrentUserWithBerechtigungen() {
            return Optional.of(currentUser);
        }

        @Override
        public Benutzer getCurrentUserWithBerechtigungen() {
            return currentUser;
        }

        @Override
        public Benutzer currentUser() {
            return currentUser;
        }

        @Override
        public Benutzer currentUserRef() {
            return currentUser;
        }
    };

    // </editor-fold>

    // <editor-fold desc="TestEntity" defaultstate="collapsed">
    static class TestEntity extends AbstractEntity<TestEntity> {
        @Serial
        private static final long serialVersionUID = -7078681254391207481L;

        @Override
        public ID<TestEntity> getId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setId(ID<TestEntity> id) {
            throw new UnsupportedOperationException();
        }
    }
    // </editor-fold>

    private final OffsetDateTime timeOnPersist = OffsetDateTime.of(
        LocalDate.of(2021, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    private final OffsetDateTime timeOnUpdate = OffsetDateTime.of(
        LocalDate.of(3000, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);
    private final ConfigService configService = Mockito.mock(ConfigService.class);
    private final AbstractEntityListener sut = new AbstractEntityListener(appClock, currentUserStub, configService);

    private final TestEntity entity = new TestEntity();

    @BeforeEach
    void when_entity_is_persisted_with_current_time_and_benutzer() {
        currentUser = benutzerOnPersist;
        clockProvider.resetUsing(timeOnPersist.toInstant());
        sut.prePersist(entity);
    }

    @Test
    void prePersist_sets_Created_and_Changed_fields_to_current_time_and_user() {
        assertThat(entity.getTimestampErstellt()).isEqualTo(timeOnPersist);
        assertThat(entity.getUserErstellt()).isEqualTo(benutzerOnPersist.getId().getId());
        assertThat(entity.getTimestampMutiert()).isEqualTo(timeOnPersist);
        assertThat(entity.getUserMutiert()).isEqualTo(benutzerOnPersist.getId().getId());
    }

    @Nested
    class when_entity_is_then_updated {
        @Test
        void preUpdate_sets_only_Changed_fields_and_retains_created_fields() {
            currentUser = benutzerOnChange;
            clockProvider.resetUsing(timeOnUpdate.toInstant());
            sut.preUpdate(entity);

            assertThat(entity.getTimestampErstellt()).isEqualTo(timeOnPersist);
            assertThat(entity.getUserErstellt()).isEqualTo(benutzerOnPersist.getId().getId());
            assertThat(entity.getTimestampMutiert()).isEqualTo(timeOnUpdate);
            assertThat(entity.getUserMutiert()).isEqualTo(benutzerOnChange.getId().getId());
        }
    }

}
