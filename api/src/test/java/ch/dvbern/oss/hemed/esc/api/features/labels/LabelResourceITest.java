package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.LabelsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class LabelResourceITest extends AbstractITest {

    private final LabelsApiSpec api = LabelsApiSpec.labels(RequestSpecUtil.quarkusSpec());

    private final LabelService labelService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllLabels() {
        // ARRANGE
        Label label = LabelBuilder.create(
            builder -> builder
                .withID(ID.random(Label.class))
                .withRandomName("Test Label")
                .withTyp(LabelTyp.NOTIZEN));
        labelService.persist(label);

        Label label2 = LabelBuilder.create(
            builder -> builder
                .withID(ID.random(Label.class))
                .withRandomName("Another Label")
                .withTyp(LabelTyp.AUFGABEN));
        labelService.persist(label2);

        // ACT
        LabelListJsonSpec result = api
            .labelResourceFindAllLabels()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LabelListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getLabelList())
            .isNotEmpty()
            .anyMatch(labelJsonSpec -> label.getId().getId().equals(labelJsonSpec.getId())
                && label.getName().getNameDe().equals(labelJsonSpec.getData().getName().getDe())
                && LabelTyp.NOTIZEN.name().equals(labelJsonSpec.getData().getLabelTyp().toString())
            )
            .anyMatch(labelJsonSpec -> label2.getId().getId().equals(labelJsonSpec.getId())
                && label2.getName().getNameDe().equals(labelJsonSpec.getData().getName().getDe())
                && LabelTyp.AUFGABEN.name().equals(labelJsonSpec.getData().getLabelTyp().toString())
            );
    }

    @Test
    @TestAsAdminBS
    void findFilteredLabels() {
        // ARRANGE
        Label label = LabelBuilder.create(
            builder -> builder
                .withID(ID.random(Label.class))
                .withRandomName("Test Label")
                .withTyp(LabelTyp.NOTIZEN));
        labelService.persist(label);

        Label label2 = LabelBuilder.create(
            builder -> builder
                .withID(ID.random(Label.class))
                .withRandomName("Another Label")
                .withTyp(LabelTyp.AUFGABEN));
        labelService.persist(label2);

        // ACT
        LabelListJsonSpec result = api
            .labelResourceFilterLabels()
            .typeQuery(LabelTyp.NOTIZEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LabelListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getLabelList())
            .isNotEmpty()
            .anyMatch(labelJsonSpec -> label.getId().getId().equals(labelJsonSpec.getId())
                && label.getName().getNameDe().equals(labelJsonSpec.getData().getName().getDe())
                && LabelTyp.NOTIZEN.name().equals(labelJsonSpec.getData().getLabelTyp().toString())
            )
            .noneMatch(labelJsonSpec -> label2.getId().getId().equals(labelJsonSpec.getId())
                && label2.getName().getNameDe().equals(labelJsonSpec.getData().getName().getDe())
                && LabelTyp.AUFGABEN.name().equals(labelJsonSpec.getData().getLabelTyp().toString())
            );
    }

    @Test
    @TestAsAdminBS
    void createLabel() {
        // ARRANGE
        String name = TestUtil.getRandomString("MyLabel");
        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder
                .withName(name)
                .withTyp(LabelTyp.NOTIZEN));

        // ACT
        LabelJsonSpec result = api
            .labelResourceCreateLabel()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(LabelJsonSpec.class);

        // ASSERT
        Optional<Label> labelReadOptional = labelService.findById(ID.of(UUID.fromString(result.getId()), Label.class));
        Assertions.assertThat(labelReadOptional).isPresent();
        Label labelRead = labelReadOptional.get();
        Assertions.assertThat(labelRead.getId()).isNotNull();
        Assertions.assertThat(labelRead.getName().getNameDe()).isEqualTo(name);
    }

    @Test
    @TestAsAdminBS
    void updateLabel() {
        // ARRANGE
        String nameBefore = TestUtil.getRandomString("LabelVorher");
        String nameUpdated = TestUtil.getRandomString("LabelNachher");
        Label label = LabelBuilder.create(
            builder -> builder
                .withName(nameBefore)
                .withTyp(LabelTyp.NOTIZEN));
        Assertions.assertThat(label.getName().getNameDe()).isEqualTo(nameBefore);
        labelService.persist(label);

        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder
                .withName(nameUpdated)
                .withTyp(LabelTyp.NOTIZEN));

        // ACT
        LabelJsonSpec result = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LabelJsonSpec.class);

        // ASSERT
        Optional<Label> labelReadOptional = labelService.findById(ID.of(UUID.fromString(result.getId()), Label.class));
        Assertions.assertThat(labelReadOptional).isPresent();
        Label labelRead = labelReadOptional.get();
        Assertions.assertThat(labelRead.getId()).isNotNull();
        Assertions.assertThat(labelRead.getName().getNameDe()).isEqualTo(nameUpdated);
    }

    @Test
    @TestAsAdminBS
    void updateLabel_throws_if_label_is_auto_label() {
        // ARRANGE
        String nameBefore = TestUtil.getRandomString("LabelVorher");
        String nameUpdated = TestUtil.getRandomString("LabelNachher");
        Label label = LabelBuilder.create(
            builder -> builder
                .withName(nameBefore)
                .isAutoLabel(true)
                .withTyp(LabelTyp.NOTIZEN));
        Assertions.assertThat(label.getName().getNameDe()).isEqualTo(nameBefore);
        labelService.persist(label);

        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder
                .withName(nameUpdated)
                .withTyp(LabelTyp.NOTIZEN));

        // ACT
        AppValidationErrorJsonSpec violation = api
            .labelResourceUpdateLabel()
            .labelIdPath(label.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(violation)
            .extractingExactlyOneViolation()
            .hasClientKey(AppValidationMessage.autoLabelsCannotBeEditedOrDeleted().getClientKey())
            .hasRawMessageContaining("Automatisch generierte Labels können nicht bearbeitet oder gelöscht werden.");
    }

    @Test
    @TestAsAdminBS
    void removeLabel() {
        // ARRANGE
        Label label = LabelBuilder.create(
            builder -> builder
                .withRandomName("Label")
                .withTyp(LabelTyp.NOTIZEN));
        labelService.persist(label);
        Optional<Label> labelOpt = labelService.findById(label.getId());
        Assertions.assertThat(labelOpt).isPresent();

        // ACT
        api
            .labelResourceRemoveLabel()
            .labelIdPath(label.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Label> labelOptAfterDelete = labelService.findById(label.getId());
        Assertions.assertThat(labelOptAfterDelete).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void removeLabel_throws_if_label_is_auto_label() {
        // ARRANGE
        Label label = LabelBuilder.create(
            builder -> builder
                .withRandomName("Label")
                .isAutoLabel(true)
                .withTyp(LabelTyp.NOTIZEN));
        labelService.persist(label);
        Optional<Label> labelOpt = labelService.findById(label.getId());
        Assertions.assertThat(labelOpt).isPresent();

        // ACT
        AppValidationErrorJsonSpec violation = api
            .labelResourceRemoveLabel()
            .labelIdPath(label.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(violation)
            .extractingExactlyOneViolation()
            .hasClientKey(AppValidationMessage.autoLabelsCannotBeEditedOrDeleted().getClientKey())
            .hasRawMessageContaining("Automatisch generierte Labels können nicht bearbeitet oder gelöscht werden.");
    }

}
