package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.InfomaterialApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
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
class InfomaterialResourceTenantITest extends AbstractITest {

    private final InfomaterialApiSpec api = InfomaterialApiSpec.infomaterial(RequestSpecUtil.quarkusSpec());

    private final InfomaterialService infomaterialService;
    private final TenantProducer tenantProducer;

    private int numBaselBeforeCreate;
    private int numBernBeforeCreate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    @Order(10)
    void create_info_as_admin_basel() {
        // ARRANGE
        this.numBaselBeforeCreate = infomaterialService.findAll().size();
        // ACT
        InfomaterialJsonSpec result = createAndRunInfomaterialRequest(TenantEnum.BS);
        // ASSERT
        ID<Infomaterial> idToload = ID.of(UUID.fromString(result.getId()), Infomaterial.class);
        var loadedInfomaterial = infomaterialService.getById(idToload);
        Assertions.assertThat(loadedInfomaterial).isNotNull();
    }

    @Test
    @TestAsAdminBE
    @Order(20)
    void create_info_ad_admin_be() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE); // Default ist BS
        this.numBernBeforeCreate = infomaterialService.findAll().size();
        // ACT
        InfomaterialJsonSpec result = createAndRunInfomaterialRequest(TenantEnum.BE);
        // ASSERT
        ID<Infomaterial> idToload = ID.of(UUID.fromString(result.getId()), Infomaterial.class);
        var loadedInfomaterial = infomaterialService.getById(idToload);
        Assertions.assertThat(loadedInfomaterial).isNotNull();
    }

    @Test
    @TestAsAdminBS
    @Order(30)
    void read_info_as_admin_basel() {
        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);
        // ASSERT
        List<InfomaterialJsonSpec> infomaterialienList = result.getInfomaterialienList();
        Assertions.assertThat(infomaterialienList).isNotNull().isNotEmpty().hasSize(numBaselBeforeCreate + 1);
    }

    @Test
    @TestAsAdminBE
    @Order(40)
    void read_info_as_admin_be() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE); // Default ist BS
        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);
        // ASSERT
        List<InfomaterialJsonSpec> infomaterialienList = result.getInfomaterialienList();
        Assertions.assertThat(infomaterialienList).isNotNull().isNotEmpty().hasSize(numBernBeforeCreate + 1);
    }

    @Test
    @TestAsAdminBS
    @Order(50)
    void create_second_bs_info() {
        // ACT
        InfomaterialJsonSpec result = createAndRunInfomaterialRequest(TenantEnum.BS);
        // ASSERT
        ID<Infomaterial> idToload = ID.of(UUID.fromString(result.getId()), Infomaterial.class);
        var loadedInfomaterial = infomaterialService.getById(idToload);
        Assertions.assertThat(loadedInfomaterial).isNotNull();
    }

    @Test
    @TestAsAdminBS
    @Order(60)
    void read_info_as_admin_basel2() {
        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);
        // ASSERT
        List<InfomaterialJsonSpec> infomaterialienList = result.getInfomaterialienList();
        Assertions.assertThat(infomaterialienList).isNotNull().isNotEmpty().hasSize(numBaselBeforeCreate + 2);
    }

    @Test
    @TestAsAdminBE
    @Order(70)
    void read_info_as_admin_bern_returns_still_1() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE); // Default ist BS
        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);
        // ASSERT
        List<InfomaterialJsonSpec> infomaterialienList = result.getInfomaterialienList();
        Assertions.assertThat(infomaterialienList).isNotNull().isNotEmpty().hasSize(numBernBeforeCreate + 1);
    }

    @NonNull
    private InfomaterialJsonSpec createAndRunInfomaterialRequest(@NonNull TenantEnum tenantEnum) {
        // ARRANGE
        tenantProducer.setCurrentTenant(tenantEnum);
        var payload = InfomaterialBuilder.createSaveJaxSpec(
            builder -> builder
                .withRandomName("Test")
        );
        // ACT
        InfomaterialJsonSpec result = api
            .infomaterialResourceCreateInfomaterial()
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(InfomaterialJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getId()).isNotNull();
        return result;
    }
}
