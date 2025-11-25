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

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum FeatureEnabledSetting {

    ENABLED,
    ENABLED_FOR_BETA_TESTING,
    ENABLED_FOR_DEVELOPMENT,
    DISABLED;

    public boolean isEnabledForDeploymentEnvironment(@NonNull DeploymentEnvironment deploymentEnvironment) {
        return switch (this) {
            case ENABLED -> true;
            case ENABLED_FOR_BETA_TESTING -> deploymentEnvironment == DeploymentEnvironment.LOCAL
                || deploymentEnvironment == DeploymentEnvironment.UNIT_TEST
                || deploymentEnvironment == DeploymentEnvironment.DEV
                || deploymentEnvironment == DeploymentEnvironment.UAT;
            case ENABLED_FOR_DEVELOPMENT -> deploymentEnvironment == DeploymentEnvironment.LOCAL
                || deploymentEnvironment == DeploymentEnvironment.UNIT_TEST
                || deploymentEnvironment == DeploymentEnvironment.DEV;
            case DISABLED -> false;
            default -> throw new IllegalStateException("Unknown FeatureEnabledSetting: " + this);
        };
    }
}
