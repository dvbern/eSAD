package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.AnlassAktivitaetHistoryListJson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.KindAktivitaetHistoryListJson;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsOrthoptistBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AktivitaetApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DossierApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.EinladungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassAktivitaetHistoryJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassAktivitaetHistoryListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindAktivitaetHistoryListJsonSpec;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class KindAktivitaetHistoryResourceITest extends AbstractITest {

    private final AktivitaetApiSpec api = AktivitaetApiSpec.aktivitaet(RequestSpecUtil.quarkusSpec());
    private final DossierApiSpec dossierApi = DossierApiSpec.dossier(RequestSpecUtil.quarkusSpec());
    private final EinladungenApiSpec einladungenApi = EinladungenApiSpec.einladungen(RequestSpecUtil.quarkusSpec());
    private final AnlassApiSpec anlassApi = AnlassApiSpec.anlass(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final ConfigService configSvc;
    private Anlass anlassOfAdmin;
    private Anlass anlassOfSchularzt;
    private List<AnlassAktivitaetHistoryJsonSpec> aktivitaetenOfAdmin; // used to check that they are not present for other users

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Mock limit in config service to make sure we get all the results. Delegate all other calls to 'real' config Service
        ConfigService configServiceMock = Mockito.spy(configSvc);
        Mockito.when(configServiceMock.getDashboardAktivitaetHistoryLimit()).thenReturn(Integer.MAX_VALUE);
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);
    }

    @Test
    @TestAsOrthoptistBS // this user is seldom used, assume it has no aktivaet
    @Order(20)
    void getAktivitaetenKind_should_return_empty_list_if_no_entry() {
        // ARRANGE
        // ACT
        KindAktivitaetHistoryListJson result = api.aktivitaetHistoryResourceFindLetzteKinder()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KindAktivitaetHistoryListJson.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindAktivitaetList()).isEmpty();
    }

    @Test
    @TestAsOrthoptistBS // this user is seldom used, assume it has no aktivaet
    @Order(30)
    void getAktivitaetenAnlaesse_should_return_empty_list_if_no_entry() {
        // ARRANGE
        // ACT
        AnlassAktivitaetHistoryListJson result = api.aktivitaetHistoryResourceFindLetzteAnlaesse()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AnlassAktivitaetHistoryListJson.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAnlassAktivitaetList()).isEmpty();
    }

    @Test
    @TestAsAdminBS
    @Order(40)
    void getAktivitaetenKinder_opening_dossier_should_create_entry() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        dossierApi.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        var result = api.aktivitaetHistoryResourceFindLetzteKinder()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KindAktivitaetHistoryListJsonSpec.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindAktivitaetList()).isNotEmpty();
        assertThat(result.getKindAktivitaetList()
            .stream()
            .anyMatch(i -> i.getKind()
                .getId()
                .equals(kind.getId().getId()))).isTrue();
    }

    @Test
    @TestAsAdminBS
    @Order(50)
    void getAktivitaetenKinder_opening_schuluntersuchung_should_create_entry() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        Dossier dossier = fixtureService.createDossier(kind);
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);

        einladungenApi.einladungResourceGetEinladungById()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        var result = api.aktivitaetHistoryResourceFindLetzteKinder()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KindAktivitaetHistoryListJsonSpec.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKindAktivitaetList()).isNotEmpty();
        assertThat(result.getKindAktivitaetList()
            .stream()
            .anyMatch(i -> i.getKind().getId().equals(kind.getId().getId()))).isTrue();
    }

    @Test
    @TestAsAdminBS
    @Order(60)
    void getAktivitaetenAnlaesse_opening_anlassplanung_should_create_entry() {
        // ARRANGE
        anlassOfAdmin = fixtureService.createAnlass();

        anlassApi.anlassResourceGetAnlass()
            .anlassIdPath(anlassOfAdmin.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        AnlassAktivitaetHistoryListJsonSpec result = api.aktivitaetHistoryResourceFindLetzteAnlaesse()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AnlassAktivitaetHistoryListJsonSpec.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAnlassAktivitaetList()).isNotEmpty();
        assertThat(result.getAnlassAktivitaetList()
            .stream()
            .anyMatch(i -> i.getAnlass().getId().equals(anlassOfAdmin.getId().getId()))).isTrue();

        aktivitaetenOfAdmin = result.getAnlassAktivitaetList();


    }

    @Test
    @TestAsSchularztBS
    @Order(70)
    void getAktivitaetenAnlaesse_opening_anlassplanung_should_create_entry_as_schularzt() {
        // ARRANGE
        anlassOfSchularzt = fixtureService.createAnlass();

        anlassApi.anlassResourceGetAnlass()
            .anlassIdPath(anlassOfSchularzt.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        AnlassAktivitaetHistoryListJsonSpec result = api.aktivitaetHistoryResourceFindLetzteAnlaesse()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AnlassAktivitaetHistoryListJsonSpec.class);
        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAnlassAktivitaetList()).isNotEmpty();
        assertThat(result.getAnlassAktivitaetList()
            .stream()
            .anyMatch(i -> i.getAnlass().getId().equals(anlassOfSchularzt.getId().getId()))).isTrue();

        assertThat(aktivitaetenOfAdmin).isNotEmpty(); // should have some admin aktivitaet from previous test
        assertThat(result.getAnlassAktivitaetList()
            .stream()
            .noneMatch(i -> aktivitaetenOfAdmin.contains(i))).isTrue();
    }

    @Test
    @TestAsSchularztBS
    @Order(80)
    void testAsSchularzt_cannot_see_admin_aktivitaeten() {
        // ARRANGE
        anlassApi.anlassResourceGetAnlass() // schularzt is allowed to open anlassOfAdmin, this will create a history entry
            .anlassIdPath(anlassOfAdmin.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        AnlassAktivitaetHistoryListJsonSpec result = api.aktivitaetHistoryResourceFindLetzteAnlaesse()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AnlassAktivitaetHistoryListJsonSpec.class);

        //ASSERT
        assertThat(result).isNotNull();
        // SA cannot see the aktivitaeten of the Admin
        assertThat(result.getAnlassAktivitaetList()
            .stream()
            .noneMatch(i -> aktivitaetenOfAdmin.contains(i))).isTrue();

        assertThat(result.getAnlassAktivitaetList()
            .stream()
            .anyMatch(i -> i.getAnlass().getId().equals(anlassOfAdmin.getId().getId()))).isTrue();
    }

}
