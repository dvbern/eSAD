package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ConfigPrintDisabledTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("app.allow-configprint", "false");
    }
}
