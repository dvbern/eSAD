package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * The legacy mode in which all tenants share one bucket (esc.s3.use.single.bucket, ESC-1086).
 */
public class S3SingleBucketTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("esc.s3.use.single.bucket", "true");
    }
}
