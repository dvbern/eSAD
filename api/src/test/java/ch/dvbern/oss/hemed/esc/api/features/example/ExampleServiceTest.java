package ch.dvbern.oss.hemed.esc.api.features.example;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ExampleServiceTest {

    private final ExampleRepo mockRepo = mock(ExampleRepo.class);
    private final ExampleService service = new ExampleService(mockRepo);

    @Test
    void receive_a_example_user() {
        var actual = service.getSomeExample();

        assertThat(actual)
            .isNotNull();
        verify(mockRepo)
            .persist(actual);
    }
}
