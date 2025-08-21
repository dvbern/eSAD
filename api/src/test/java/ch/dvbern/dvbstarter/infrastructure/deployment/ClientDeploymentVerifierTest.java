package ch.dvbern.dvbstarter.infrastructure.deployment;

import com.vdurmont.semver4j.Semver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ClientDeploymentVerifierTest {

    private final ClientDeploymentVerifier sut = ClientDeploymentVerifier.createDefault(
        new Semver("1.0.0")
    );

    @Test
    void for_matching_version_and_environment_it_accepts() {
        var actual = sut.clientIsCompatible("1.0.0");

        assertThat(actual)
            .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.0.1",
        "1.1.0",
        "2.0.0",
        "1.0.1-alpha",
        "1.0.1+build",
        "a",
        "",
    })
    @NullSource
    void for_invalid_versions_it_rejects(@Nullable String version) {
        var actual = sut.clientIsCompatible(version);

        assertThat(actual)
            .isFalse();
    }

    @Nested
    class with {
        @SuppressWarnings("InnerClassFieldHidesOuterClassField")
        private final ClientDeploymentVerifier sut = ClientDeploymentVerifier.createDefault(
                new Semver("1.0.0")
            )
            .withVersionMatcher((server, client) -> client.isEqualTo(new Semver("9.9.9")));

        @Test
        void it_accepts_using_custom_env_matchers() {
            var actual = sut.clientIsCompatible("9.9.9");

            assertThat(actual)
                .isTrue();
        }
    }
}
