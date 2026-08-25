package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminWithInvalidTenant;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.LabelsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TranslatedStringJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabelResourceIMandantITest extends AbstractITest {

    private final LabelResourceHelper labelResourceHelper;
    private final TenantProducer tenantProducer;

    private final LabelsApiSpec api = LabelsApiSpec.labels(RequestSpecUtil.quarkusSpec());

    private String createdLabelBaselId;
    private String createdLabelWinId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminWithInvalidTenant
    @Order(10)
    void making_a_request_with_an_invalid_tenant_should_fail() {
        // ARRANGE
        String name = TestUtil.getRandomString("MyLabel");
        LabelSaveJsonSpec saveJson = LabelBuilder.createSaveJaxSpec(
            builder -> builder
                .withName(name)
                .withTyp(LabelTyp.NOTIZEN));

        // ACT
        api
            .labelResourceCreateLabel()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()); // is now unauthorized
    }

    @Test
    @TestAsAdminBS
    @Order(20)
    void create_label_as_admin_basel() {
        LabelJsonSpec createdForBasel = labelResourceHelper.createNewLabel("CreatedForBasel", LabelTyp.NOTIZEN);
        Assertions.assertThat(createdForBasel).isNotNull();
        createdLabelBaselId = createdForBasel.getId();
    }

    @Test
    @TestAsAdminBE
    @Order(30)
    void create_label_as_admin_be() {
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        LabelJsonSpec createdForBern = labelResourceHelper.createNewLabel("CreatedForBern", LabelTyp.NOTIZEN);
        Assertions.assertThat(createdForBern).isNotNull();
        createdLabelWinId = createdForBern.getId();
    }

    @Test
    @TestAsAdminBS
    @Order(40)
    void read_filtered_labels_as_admin_basel() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        // ACT
        var result = api
            .labelResourceFilterLabels()
            .typeQuery(LabelTyp.NOTIZEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LabelListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getLabelList()).isNotNull()
            .asInstanceOf(InstanceOfAssertFactories.list(LabelJsonSpec.class))
            .extracting(LabelJsonSpec::getId)
            .contains(createdLabelBaselId)
            .doesNotContain(createdLabelWinId);
    }

    @Test
    @TestAsAdminBS
    @Order(50)
    void read_all_labels_as_admin_basel() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        // ACT
        var result = api
            .labelResourceFindAllLabels()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LabelListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getLabelList()).isNotNull()
            .asInstanceOf(InstanceOfAssertFactories.list(LabelJsonSpec.class))
            .extracting(LabelJsonSpec::getId)
            .contains(createdLabelBaselId)
            .doesNotContain(createdLabelWinId);
    }

    @Test
    @TestAsSupportBS
    @Order(60)
    void updateLabel_should_update_user_mutiert() {
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        var result = api.labelResourceGetById()
            .labelIDPath(createdLabelBaselId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LabelJsonSpec.class);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(createdLabelBaselId);

        // ACT
        var loadedLabel = result.getData();
        UUID uuid = UUID.randomUUID();
        loadedLabel.getName().setDe("MyLabelNameChange_" + uuid);

        LabelJsonSpec changedLabel  = api.labelResourceUpdateLabel()
            .body(loadedLabel)
            .labelIdPath(createdLabelBaselId)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LabelJsonSpec.class);

        // ASSERT
        Assertions.assertThat(changedLabel).isNotNull()
            .extracting(LabelJsonSpec::getData)
            .isNotNull()
            .extracting(LabelSaveJsonSpec::getName)
            .isNotNull()
            .extracting(TranslatedStringJsonSpec::getDe).isEqualTo("MyLabelNameChange_" + uuid);

        Assertions.assertThat(changedLabel.getEntityInfo().getUserErstellt()).isNotNull().isEqualTo(TestAsAdminBS.BENUTZER_ID);
        Assertions.assertThat(changedLabel.getEntityInfo().getUserMutiert()).isNotNull().isEqualTo(TestAsSupportBS.BENUTZER_ID);

    }
}
