package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.InfomaterialApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class InfomaterialResourceITest extends AbstractITest {

    private final InfomaterialApiSpec api = InfomaterialApiSpec.infomaterial(RequestSpecUtil.quarkusSpec());

    private final InfomaterialService infomaterialService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllInfomaterialien() {
        // ARRANGE
        ID<Infomaterial> infomaterialID = ID.random(Infomaterial.class);
        Infomaterial infomaterial = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID)
                .withRandomName("InfomaterialForTest")
                .isActive(true));
        infomaterialService.persist(infomaterial);

        ID<Infomaterial> infomaterialID2 = ID.random(Infomaterial.class);
        Infomaterial infomaterial2 = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID2)
                .withRandomName("InfomaterialForTest2")
                .isActive(true));
        infomaterialService.persist(infomaterial2);

        ID<Infomaterial> infomaterialID3 = ID.random(Infomaterial.class);
        Infomaterial infomaterial3 = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID3)
                .withRandomName("InfomaterialForTest3")
                .isActive(false));
        infomaterialService.persist(infomaterial3);

        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .onlyActiveQuery(false)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getInfomaterialienList())
            .isNotEmpty()
            .extracting(InfomaterialJsonSpec::getId)
            .contains(
                infomaterialID.getId(),
                infomaterialID2.getId(),
                infomaterialID3.getId()
            );
    }

    @Test
    @TestAsAdminBS
    void findAllActiveInfomaterialien() {
        // ARRANGE
        ID<Infomaterial> infomaterialID = ID.random(Infomaterial.class);
        Infomaterial infomaterial = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID)
                .withRandomName("InfomaterialForTest")
                .isActive(true));
        infomaterialService.persist(infomaterial);

        ID<Infomaterial> infomaterialID2 = ID.random(Infomaterial.class);
        Infomaterial infomaterial2 = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID2)
                .withRandomName("InfomaterialForTest2")
                .isActive(true));
        infomaterialService.persist(infomaterial2);

        ID<Infomaterial> infomaterialID3 = ID.random(Infomaterial.class);
        Infomaterial infomaterial3 = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID3)
                .withRandomName("InfomaterialForTest3")
                .isActive(false));
        infomaterialService.persist(infomaterial3);

        // ACT
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .onlyActiveQuery(true)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getInfomaterialienList())
            .isNotEmpty()
            .extracting(InfomaterialJsonSpec::getId)
            .contains(
                infomaterialID.getId(),
                infomaterialID2.getId()
            );
        Assertions.assertThat(result.getInfomaterialienList())
            .noneMatch(info -> info.getId().equals(infomaterialID3.getId()));
    }

    @Test
    @TestAsAdminBS
    void createInfomaterial() {
        // ARRANGE
        ID<Infomaterial> infomaterialID = ID.random(Infomaterial.class);
        String name = TestUtil.getRandomString("InfomaterialForTest");
        InfomaterialSaveJsonSpec payload = InfomaterialBuilder.createSaveJaxSpec(
            builder -> builder
                .withID(infomaterialID)
                .withName(name));

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
        Optional<Infomaterial> infomaterialOptional =
            infomaterialService.findById(ID.of(UUID.fromString(result.getId()), Infomaterial.class));
        Assertions.assertThat(infomaterialOptional).isPresent();
        Infomaterial infomaterial = infomaterialOptional.get();
        Assertions.assertThat(infomaterial.getId()).isNotNull();
        Assertions.assertThat(infomaterial.getName().getNameDe()).isEqualTo(name);
    }

    @Test
    @TestAsAdminBS
    void updateInfomaterial() {
        // ARRANGE
        String nameBefore = TestUtil.getRandomString("InfomaterialNameBefore");
        String nameAfter = TestUtil.getRandomString("InfomaterialNameAfter");
        ID<Infomaterial> infomaterialID = ID.random(Infomaterial.class);
        Infomaterial infomaterial = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID)
                .withName(nameBefore));
        Assertions.assertThat(infomaterial.getName().getNameDe()).isEqualTo(nameBefore);
        infomaterialService.persist(infomaterial);

        InfomaterialSaveJsonSpec saveJson = InfomaterialBuilder.createSaveJaxSpec(
            builder -> builder
                .withID(infomaterialID)
                .withName(nameAfter));

        // ACT
        InfomaterialJsonSpec result = api
            .infomaterialResourceUpdateInfomaterial()
            .infomaterialIdPath(infomaterial.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialJsonSpec.class);

        // ASSERT
        Optional<Infomaterial> infomaterialOptional =
            infomaterialService.findById(ID.of(UUID.fromString(result.getId()), Infomaterial.class));
        Assertions.assertThat(infomaterialOptional).isPresent();
        Infomaterial infomaterialUpdated = infomaterialOptional.get();
        Assertions.assertThat(infomaterial.getId()).isNotNull();
        Assertions.assertThat(infomaterialUpdated.getName().getNameDe()).isEqualTo(nameAfter);
    }

    @Test
    @TestAsAdminBS
    void removeInfomaterial() {
        // ARRANGE
        ID<Infomaterial> infomaterialID = ID.random(Infomaterial.class);
        Infomaterial infomaterial = InfomaterialBuilder.create(
            builder -> builder
                .withID(infomaterialID)
                .withRandomName("InfomaterialForTest"));
        infomaterialService.persist(infomaterial);
        Optional<Infomaterial> infomaterialOptional = infomaterialService.findById(infomaterial.getId());
        Assertions.assertThat(infomaterialOptional).isPresent();

        // ACT
        api.infomaterialResourceRemoveInfomaterial()
            .infomaterialIdPath(infomaterial.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Infomaterial> infomaterialRemoved = infomaterialService.findById(infomaterial.getId());
        Assertions.assertThat(infomaterialRemoved).isNotPresent();
    }
}
