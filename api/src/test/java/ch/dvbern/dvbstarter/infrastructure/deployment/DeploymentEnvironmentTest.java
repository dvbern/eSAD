package ch.dvbern.dvbstarter.infrastructure.deployment;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DeploymentEnvironmentTest {

    @Test
    void enum_contains_all_expected_entries() {
        assertThat(DeploymentEnvironment.values())
            .containsExactly(
                DeploymentEnvironment.LOCAL,
                DeploymentEnvironment.UNIT_TEST,
                DeploymentEnvironment.DEV,
                DeploymentEnvironment.REVIEW,
                DeploymentEnvironment.IAT,
                DeploymentEnvironment.UAT,
                DeploymentEnvironment.DEMO,
                DeploymentEnvironment.PROD
            );
    }

    @ParameterizedTest
    @CsvSource({
        "LOCAL, false",
        "UNIT_TEST, false",
        "DEV, true",
        "REVIEW, true",
        "IAT, true",
        "UAT, true",
        "DEMO, true",
        "PROD, true",
    })
    void version_filter_is_enabled_for_non_testing_environments_only(DeploymentEnvironment env, boolean expected) {
        assertThat(env.isVersionFilterEnabled())
            .isEqualTo(expected);
    }
}
