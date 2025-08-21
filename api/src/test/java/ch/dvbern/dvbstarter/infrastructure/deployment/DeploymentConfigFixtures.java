package ch.dvbern.dvbstarter.infrastructure.deployment;

import ch.dvbern.oss.hemed.esc.api.Util;
import java.net.URL;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeploymentConfigFixtures {

    @Value
    @Accessors(fluent = true)
    @With
    public static class DeploymentConfigDTO {
        private final String appProject;
        private final String appModule;
        private final DeploymentEnvironment environment;
        private final String instance;
        private final URL publicUrl;
        private final String version;
        private final ch.dvbern.dvbstarter.infrastructure.deployment.ClientAuthConfigDTO clientAuthConfig;
    }

    @Value
    @Accessors(fluent = true)
    @With
    public static class ClientAuthConfigDTO
        implements ch.dvbern.dvbstarter.infrastructure.deployment.ClientAuthConfigDTO {
        private final String clientId;
        private final String realm;
        private final URL authUrl;
    }

    public static DeploymentConfigDTO defaults() {
        return new DeploymentConfigDTO(
            "unit-test-project",
            "unit-test-app-module",
            DeploymentEnvironment.UNIT_TEST,
            "unit-test-fixture",
            Util.parseURL("http://localhost:8080"),
            new String("1.0.0"),
            new ClientAuthConfigDTO("unit-test-client-id", "unit-test-realm", Util.parseURL("http://localhost:8080"))
        );
    }
}
