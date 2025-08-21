package ch.dvbern.oss.hemed.esc.api.shared.abstractentity;

import java.io.Serial;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerFixtureBuilder;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AbstractEntityTest {

    @Getter
    @Setter
    @AllArgsConstructor
    static class TestAbstractEntity extends AbstractEntity<TestAbstractEntity> {
        @Serial
        private static final long serialVersionUID = 1757990011744172987L;
        @Id
        private ID<TestAbstractEntity> id;
    }

    // <editor-fold desc="EqualsAndHashCode" defaultstate="collapsed">
    @Nested
    class EqualsAndHashCode {
        private final Benutzer benutzer = BenutzerFixtureBuilder.validBenutzer()
            .withId(ID.parse("99999999-9999-9999-9999-999999999999", Benutzer.class))
            .build();

        private final Benutzer otherBenutzer = BenutzerFixtureBuilder.validBenutzer()
            .withId(ID.parse("55555555-5555-5555-5555-555555555555", Benutzer.class))
            .build();
        private final OffsetDateTime now = OffsetDateTime.parse("2021-01-01T00:00:00Z");

        private final ID<TestAbstractEntity> id =
            ID.parse("11111111-1111-1111-1111-111111111111", TestAbstractEntity.class);
        private final TestAbstractEntity sut = new TestAbstractEntity(id)
            .setVersion(0)
            .setTimestampErstellt(now)
            .setUserErstellt(benutzer.getId().getId())
            .setTimestampMutiert(now)
            .setUserMutiert(benutzer.getId().getId());

        @Test
        void equals_and_hashcode_use_only_id() {
            TestAbstractEntity otherWithSameId = new TestAbstractEntity(id)
                .setVersion(1)
                .setTimestampErstellt(now.plusDays(1))
                .setUserErstellt(otherBenutzer.getId().getId())
                .setTimestampMutiert(now.plusDays(1))
                .setUserMutiert(otherBenutzer.getId().getId());

            assertThat(sut)
                .isEqualTo(otherWithSameId);
            assertThat(sut.hashCode())
                .hasSameHashCodeAs(otherWithSameId);
        }

        @Test
        void equals_and_hashcode_differ_on_differing_id() {
            ID<TestAbstractEntity> otherId = ID.parse("bbb0e3f8-694b-4e51-9350-46a3e911d26a", TestAbstractEntity.class);
            TestAbstractEntity otherWithDifferentId = new TestAbstractEntity(otherId)
                .setVersion(sut.getVersion())
                .setTimestampErstellt(sut.getTimestampErstellt())
                .setUserErstellt(sut.getUserErstellt())
                .setTimestampMutiert(sut.getTimestampMutiert())
                .setUserMutiert(sut.getUserMutiert());

            assertThat(sut)
                .isNotEqualTo(otherWithDifferentId);
            assertThat(sut.hashCode())
                .isNotEqualTo(otherWithDifferentId.hashCode());
        }

        @Test
        void equals_returns_true_for_same_instance() {
            //noinspection EqualsWithItself
            assertThat(sut)
                .isEqualTo(sut);
        }

        @Test
        void equals_returns_false_for_null() {
            assertThat(sut)
                .isNotEqualTo(null);
        }

        @Test
        void equals_returns_false_for_different_class() {
            assertThat(sut)
                .isNotEqualTo(new Object());
        }
    }
    // </editor-fold>

    // <editor-fold desc="GetSilent" defaultstate="collapsed">
    @Nested
    class GetSilent {
        private final TestAbstractEntity sut = new TestAbstractEntity(ID.random(TestAbstractEntity.class));

        @Test
        void returns_null_when_value_is_null() {
            Object actual = sut.getSilent(() -> null);

            assertThat(actual)
                .isEqualTo("null");
        }

        @Test
        void returns_value_when_value_is_not_null() {
            Object actual = sut.getSilent(() -> "Hello");

            assertThat(actual)
                .isEqualTo("Hello");
        }

        @Test
        void does_not_throw_if_supplier_throws() {
            Object actual = sut.getSilent(() -> {
                throw new RuntimeException("Hello");
            });

            assertThat(actual)
                .isEqualTo("<unknown>");
        }
    }
    // </editor-fold>
}
