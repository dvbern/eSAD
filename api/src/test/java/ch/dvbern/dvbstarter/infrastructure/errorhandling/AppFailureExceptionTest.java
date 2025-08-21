package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import java.io.Serial;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage.entityNotFound;
import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage.internalError;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppFailureExceptionAssert.assertThatAppFailureException;

@SuppressWarnings("ResultOfMethodCallIgnored")
class AppFailureExceptionTest {

    private static final UUID FAKE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @AllArgsConstructor
    @Getter
    @Setter
    private static class FakeEntity extends AbstractEntity<FakeEntity> {
        @Serial
        private static final long serialVersionUID = 6987705652622093204L;
        private ID<FakeEntity> id;
    }

    @Test
    void building_an_exception_has_a_unique_id_set() {
        var actual = new AppFailureException(internalError("some text"));

        assertThatAppFailureException(actual)
            .hasId();
    }

    @Test
    void entityNotFound_with_entity_and_id() {

        var actual =
            new AppFailureException(entityNotFound(ID.of(FAKE_ID, FakeEntity.class)));

        assertThatAppFailureException(actual)
            .hasI18nMessage(I18nMessage.of(
                "AppFailure.ENTITY_NOT_FOUND",
                "entityName",
                "FakeEntity",
                "id",
                "11111111-1111-1111-1111-111111111111"
            ));
    }

    @Test
    void entityNotFound_with_entity_and_id_and_message() {
        var actual =
            new AppFailureException(
                AppFailureMessage.entityNotFound(ID.of(FAKE_ID, FakeEntity.class), "some message")
            );

        assertThatAppFailureException(actual)
            .hasI18nMessage(I18nMessage.of(
                "AppFailure.ENTITY_NOT_FOUND_WITH_MESSAGE",
                "entityName",
                "FakeEntity",
                "id",
                "11111111-1111-1111-1111-111111111111",
                "message",
                "some message"
            ));
    }

    @Test
    void entityNotFound_with_ID() {
        ID<?> id = ID.parse("11111111-1111-1111-1111-111111111111", FakeEntity.class);
        var actual =
            new AppFailureException(entityNotFound(id));

        assertThatAppFailureException(actual)
            .hasI18nMessage(I18nMessage.of(
                "AppFailure.ENTITY_NOT_FOUND",
                "entityName",
                "FakeEntity",
                "id",
                "11111111-1111-1111-1111-111111111111"
            ));
    }


}
