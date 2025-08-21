package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.net.URL;

import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckAllowedLinkValidatorTest {

    private final ConfigService configSericeMock = mock(ConfigService.class);

    @BeforeEach
    void setUp() {
        when(configSericeMock.getPublicUrl()).thenReturn(Util.parseURL("https://esad.ch"));
    }

    @Test
    void setLink_happycase() {
        // ARRANGE
        URL link = Util.parseURL("https://esad.ch/myspecialurl");
        // ACT
        CheckAllowedLinkValidator validator = new CheckAllowedLinkValidator(configSericeMock);
        // ASSERT
        assertThat(validator.isValid(link, null)).isTrue();
    }

    @Test
    void setLink_urlDoesntMatchOurDomain_throwsException() {
        // ARRANGE
        URL link = Util.parseURL("https://bad-domain.hack/hehe");
        // ACT
        CheckAllowedLinkValidator validator = new CheckAllowedLinkValidator(configSericeMock);
        // ASSERT
        assertThat(validator.isValid(link, null)).isFalse();
    }
}
