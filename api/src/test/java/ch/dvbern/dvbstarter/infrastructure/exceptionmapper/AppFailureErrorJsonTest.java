package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.ExceptionId;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.FailureType;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppFailureErrorJson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppFailureErrorJsonTest {
    private final ExceptionId id = ExceptionId.fromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Test
    void building_with_summary_only() {
        var actual = new AppFailureErrorJson(id, "asdf");

        assertThat(actual.getType())
            .isEqualTo(FailureType.FAILURE);
        assertThat(actual.getId())
            .isEqualTo(id);
        assertThat(actual.getSummary())
            .isEqualTo("asdf");
    }

    @Test
    void building_with_summary_and_detail() {
        var actual = new AppFailureErrorJson(
            id,
            "asdf",
            "fdsa"
        );

        assertThat(actual.getType())
            .isEqualTo(FailureType.FAILURE);
        assertThat(actual.getId())
            .isEqualTo(id);
        assertThat(actual.getSummary())
            .isEqualTo("asdf, detail: fdsa");
    }

    @Test
    void building_with_summary_and_empty_references() {
        var actual = new AppFailureErrorJson(
            id,
            "asdf",
            ""
        );

        assertThat(actual.getType())
            .isEqualTo(FailureType.FAILURE);
        assertThat(actual.getId())
            .isEqualTo(id);
        assertThat(actual.getSummary())
            .isEqualTo("asdf, detail: ");
    }

    @Test
    void building_with_summary_and_some_references() {
        var stub = new Object();

        var actual = new AppFailureErrorJson(
            id,
            "asdf",
            "asdf.1.fdsa"
        );

        assertThat(actual.getType())
            .isEqualTo(FailureType.FAILURE);
        assertThat(actual.getId())
            .isEqualTo(id);
        assertThat(actual.getSummary())
            .isEqualTo("asdf, detail: asdf.1.fdsa");
    }

}
