/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.settings.features;

import java.util.EnumSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureEnabledSettingTest {

    @Test
    void enabled_isTrueForAllEnvironments() {
        for (DeploymentEnvironment env : DeploymentEnvironment.values()) {
            boolean result = FeatureEnabledSetting.ENABLED.isEnabledForDeploymentEnvironment(env);
            assertThat(result)
                .as("ENABLED should be true for environment %s", env)
                .isTrue();
        }
    }

    @Test
    void enabledForTesting_isTrueOnlyForTestingEnvironments() {
        Set<DeploymentEnvironment> allowed = EnumSet.of(
            DeploymentEnvironment.LOCAL,
            DeploymentEnvironment.UNIT_TEST,
            DeploymentEnvironment.DEV,
            DeploymentEnvironment.UAT
        );

        for (DeploymentEnvironment env : DeploymentEnvironment.values()) {
            boolean result = FeatureEnabledSetting.ENABLED_FOR_BETA_TESTING.isEnabledForDeploymentEnvironment(env);
            if (allowed.contains(env)) {
                assertThat(result)
                    .as("ENABLED_FOR_TESTING should be true for environment %s", env)
                    .isTrue();
            } else {
                assertThat(result)
                    .as("ENABLED_FOR_TESTING should be false for environment %s", env)
                    .isFalse();
            }
        }
    }

    @Test
    void enabledForDevelopment_isTrueOnlyForDevelopmentEnvironments() {
        Set<DeploymentEnvironment> allowed = EnumSet.of(
            DeploymentEnvironment.LOCAL,
            DeploymentEnvironment.UNIT_TEST,
            DeploymentEnvironment.DEV
        );

        for (DeploymentEnvironment env : DeploymentEnvironment.values()) {
            boolean result = FeatureEnabledSetting.ENABLED_FOR_DEVELOPMENT.isEnabledForDeploymentEnvironment(env);
            if (allowed.contains(env)) {
                assertThat(result)
                    .as("ENABLED_FOR_DEVELOPMENT should be true for environment %s", env)
                    .isTrue();
            } else {
                assertThat(result)
                    .as("ENABLED_FOR_DEVELOPMENT should be false for environment %s", env)
                    .isFalse();
            }
        }
    }

    @Test
    void disabled_isFalseForAllEnvironments() {
        for (DeploymentEnvironment env : DeploymentEnvironment.values()) {
            boolean result = FeatureEnabledSetting.DISABLED.isEnabledForDeploymentEnvironment(env);
            assertThat(result)
                .as("DISABLED should be false for environment %s", env)
                .isFalse();
        }
    }

}
