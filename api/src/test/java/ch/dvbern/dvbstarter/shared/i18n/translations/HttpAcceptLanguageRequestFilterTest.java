package ch.dvbern.dvbstarter.shared.i18n.translations;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class HttpAcceptLanguageRequestFilterTest {

    private final ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
    private final HttpAcceptLanguageRequestFilter sut = new HttpAcceptLanguageRequestFilter();

    @Test
    void parses_header_containing_exact_match_into_correct_sequence() {
        Mockito.doReturn("de-CH,de;q=0.9,en-US;q=0.8,en;q=0.7,fr;q=0.6")
            .when(requestContext)
            .getHeaderString("Accept-Language");

        sut.filter(requestContext);

        assertThat(sut.getAppLanguage())
            .hasValue(AppLanguages.DE);
    }

    @Test
    void parses_header_with_other_priority_into_correct_sequence() {
        Mockito.doReturn("en-US,en;q=0.9,de-CH;q=0.8,de;q=0.7,fr;q=0.6")
            .when(requestContext)
            .getHeaderString("Accept-Language");

        sut.filter(requestContext);

        assertThat(sut.getAppLanguage())
            .hasValue(AppLanguages.EN);
    }

    @Test
    void parses_empty_header_into_default_language() {
        Mockito.doReturn("")
            .when(requestContext)
            .getHeaderString("Accept-Language");

        sut.filter(requestContext);

        assertThat(sut.getAppLanguage())
            .hasValue(AppLanguages.DEFAULT);
    }

}
