package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.FailureType;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppValidationErrorJson;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.ViolationJson;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AppValidationErrorJsonTest {

    @Test
    void building_with_Reference_list() {
        var actual = new AppValidationErrorJson(List.of(
            ViolationJson.fromReferenceList(
                "Blah Fasel",
                "blah.fasel",
                List.of(
                    new Reference(new Object(), "asdf"),
                    new Reference(new Object(), 1),
                    new Reference(new Object(), "fdsa")
                )
            ),
            ViolationJson.of("foo.bar.baz", "Min", "other message")
        ));

        assertThat(actual.getType())
            .isEqualTo(FailureType.VALIDATION);
        assertThat(actual.getViolations())
            .containsExactlyInAnyOrder(
                ViolationJson.of("asdf.1.fdsa", "blah.fasel", "Blah Fasel"),
                ViolationJson.of("foo.bar.baz", "Min", "other message")
            );
    }

    @Test
    void building_with_ConstraintViolations() {
        var violation1 = ViolationJson.of("asdf.1.fdsa", "Size", "some message");
        var violation2 = ViolationJson.of("foo.bar.baz", "Min", "other message");

        var actual = new AppValidationErrorJson(List.of(
            violation1,
            violation2
        ));

        assertThat(actual.getViolations())
            .containsExactlyInAnyOrder(
                ViolationJson.of("asdf.1.fdsa", "Size", "some message"),
                ViolationJson.of("foo.bar.baz", "Min", "other message")
            );
    }

}
