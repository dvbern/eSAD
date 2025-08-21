package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SuperAdminRunnerApiSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class SuperAdminRunnerResourceITest extends AbstractITest {

    private final SuperAdminRunnerApiSpec api =
        SuperAdminRunnerApiSpec.superAdminRunner(RequestSpecUtil.quarkusSpec());

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsSupport
    void triggerDocumentQueueJob() {
        // ARRANGE

        // ACT & ASSERT
        api.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @TestAsSupport
    void triggerDocumentQueueCleanUpJob() {
        // ARRANGE

        // ACT & ASSERT
        api.superAdminRunnerResourceTriggerDocumentQueueCleanUpJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @TestAsSupport
    void triggerBenutzerInaktivierenJob() {
        // ARRANGE

        // ACT & ASSERT
        api.superAdminRunnerResourceTriggerBenutzerInaktivierenJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @TestAsSupport
    void triggerKinderInaktivierenJob() {
        // ARRANGE

        // ACT & ASSERT
        api.superAdminRunnerResourceTriggerKinderInaktivierenJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }
}
