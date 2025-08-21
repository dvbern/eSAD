package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.QuartierService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.QuartierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.QuartiereApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.QuartierListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.QuartierOptionListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
class QuartierResourceITest extends AbstractITest {

    private final QuartiereApiSpec api = QuartiereApiSpec.quartiere(RequestSpecUtil.quarkusSpec());

    private final QuartierService quartierService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Order(10)
    @Test
    @TestAsAdminBS
    void findAllQuartiere() {
        // ARRANGE
        final String gbw = "GBW";
        final String gbo = "GBO";
        final String kb = "KB";
        final String land = "LAND";

        // ACT
        QuartierListJsonSpec result = api
            .quartierResourceFindAllQuartiere()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(QuartierListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getQuartierJsonList()).hasSize(4);
        Assertions.assertThat(result.getQuartierJsonList())
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getData().getQuartierName().equals(gbw))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getData().getQuartierName().equals(gbo))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getData().getQuartierName().equals(kb))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getData().getQuartierName().equals(land));
    }

    @Order(20)
    @Test
    @TestAsAdminBS
    void findAllQuartierOptions() {
        // ARRANGE
        final String gbw = "GBW";
        final String gbo = "GBO";
        final String kb = "KB";
        final String land = "LAND";

        // ACT
        QuartierOptionListJsonSpec result = api
            .quartierResourceFindAllQuartierOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(QuartierOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getQuartiereList()).hasSize(4);
        Assertions.assertThat(result.getQuartiereList())
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getQuartierName().equals(gbw))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getQuartierName().equals(gbo))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getQuartierName().equals(kb))
            .anyMatch(quartierJsonSpec -> quartierJsonSpec.getQuartierName().equals(land));
    }

    @Order(30)
    @Test
    @TestAsAdminBS
    void findQuartierById() {
        // ARRANGE
        final ID<Quartier> quartierID = ID.of(UUID.fromString("11111111-1111-2222-3333-111111111101"), Quartier.class);

        // ACT
        Quartier quartier = quartierService.getById(quartierID);

        // ASSERT
        Assertions.assertThat(quartier).isNotNull();
        Assertions.assertThat(quartier.getId()).isEqualTo(quartierID);
    }

    @Order(40)
    @Test
    @TestAsAdminBS
    void persistedQuartierExists() {
        // ARRANGE
        final ID<Quartier> quartierID = ID.of(
            UUID.fromString("11111111-1111-2222-3333-111111111105"),
            Quartier.class);
        final String quartierName = "TestQuartier";
        final Quartier quartier = QuartierBuilder.create(
            builder -> builder
                .withID(quartierID)
                .withQuartier(quartierName));
        final Quartier quartierPersisted = quartierService.persist(quartier);

        // ACT
        Quartier createdQuartier = quartierService.getById(quartierPersisted.getId());

        // ASSERT
        Assertions.assertThat(createdQuartier).isNotNull();
        Assertions.assertThat(createdQuartier.getId()).isEqualTo(quartierID);
        Assertions.assertThat(createdQuartier.getQuartierName()).isEqualTo(quartierName);
    }

    @Order(50)
    @Test
    @TestAsAdminBS
    void removeTestQuartier() {
        // ARRANGE
        final ID<Quartier> quartierID = ID.of(
            UUID.fromString("11111111-1111-2222-3333-111111111105"),
            Quartier.class);

        // ACT
        quartierService.remove(quartierID);
        Optional<Quartier> optionalQuartier = quartierService.findById(quartierID);

        // ASSERT
        Assertions.assertThat(optionalQuartier).isNotPresent();

    }
}
