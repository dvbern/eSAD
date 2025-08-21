package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonMappingReferenceParserTest {
    private final Object stub = new Object();

    @Nested
    class FromReferenceList {

        @Test
        void converts_Reference() {
            var referenceList = List.of(
                new Reference(stub, "asdf"),
                new Reference(stub, 1),
                new Reference(stub, "fdsa")
            );

            var actual = JsonMappingReferenceParser.fromRefrenceList(referenceList)
                .parse();

            assertThat(actual)
                .isEqualTo("asdf.1.fdsa");
        }
    }

}
