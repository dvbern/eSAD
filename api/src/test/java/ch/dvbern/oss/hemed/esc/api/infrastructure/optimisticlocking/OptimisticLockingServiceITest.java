package ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.LabelsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class OptimisticLockingServiceITest extends AbstractITest {

    private final LabelsApiSpec api = LabelsApiSpec.labels(RequestSpecUtil.quarkusSpec());

    private final LabelService labelService;

    @Test
    @TestAsAdminBS
    void updateLabel() {
        // ARRANGE
        Label label = labelService.persist(LabelBuilder.defaultLabel());
        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder.forLabel(label));

        // ACT
        // Update 1 - this should work
        saveJson.setVersion(0L);
        saveJson.getName().setDe(TestUtil.getRandomString("OptimisticLocking: Update 1"));
        LabelJsonSpec result = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LabelJsonSpec.class);
        // Update 2 - should fail due to optimistic locking
        saveJson.setVersion(0L); // Still the same version
        saveJson.getName().setDe(TestUtil.getRandomString("OptimisticLocking: Update 2"));
        var error = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Label labelRead = labelService.getById(ID.of(UUID.fromString(result.getId()), Label.class));
        Assertions.assertThat(labelRead.getName().getNameDe()).contains("OptimisticLocking: Update 1");
        Assertions.assertThat(error).isNotNull().extracting(AppFailureErrorJsonSpec::getSummary)
            .isEqualTo("Die Daten wurden durch einen anderen Benutzer geändert. Bitte die Ansicht aktualisieren (F5).");
    }
}
