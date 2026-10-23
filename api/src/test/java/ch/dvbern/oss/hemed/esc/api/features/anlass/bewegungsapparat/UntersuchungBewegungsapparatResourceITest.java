package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat;

import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBewegungsapparatBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungBewegungsapparatApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SkolioseSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungBewegungsapparatResourceITest extends AbstractITest {

    private final UntersuchungBewegungsapparatApiSpec api =
        UntersuchungBewegungsapparatApiSpec.untersuchungBewegungsapparat(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungBewegungsapparatService untersuchungBewegungsapparatService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungBewegungsapparat_non_existing_should_return_empty_response() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(HasUntersuchungIDWrapper.fromEinladungId(einladung.getId()));
        // ACT
        api
            .untersuchungBewegungsapparatResourceGetUntersuchungBewegungsapparat()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .body(emptyOrNullString());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungBewegungsapparat_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithBewegungsapparat();
        assertThat(untersuchung.getUntersuchungBewegungsapparat()).isNotNull();

        UntersuchungBewegungsapparat existingBewegungsapparat =
            untersuchungBewegungsapparatService.getById(untersuchung.getUntersuchungBewegungsapparat().getId());

        assertThat(existingBewegungsapparat).isNotNull();
        assertThat(existingBewegungsapparat.getSkoliose()).isNotNull();
        assertThat(existingBewegungsapparat.getHaltungsschwaeche()).isNotNull();

        // ACT
        UntersuchungBewegungsapparatJsonSpec result = api
            .untersuchungBewegungsapparatResourceGetUntersuchungBewegungsapparat()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingBewegungsapparat.getId().getId());
        assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingBewegungsapparat.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungBewegungsapparat() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithBewegungsapparat();
        assertThat(untersuchung.getUntersuchungBewegungsapparat()).isNotNull();

        UntersuchungBewegungsapparat existingBewegungsapparat =
            untersuchungBewegungsapparatService.getById(untersuchung.getUntersuchungBewegungsapparat().getId());

        assertThat(existingBewegungsapparat).isNotNull();
        assertThat(existingBewegungsapparat.getBemerkungen())
            .isNotNull()
            .isEqualTo("Bewegungsapparat Bemerkungen SU");
        assertThat(existingBewegungsapparat.getSkoliose()).isNotNull();
        assertThat(existingBewegungsapparat.getSkoliose().getEnumValue(Skoliose.class)).isEqualTo(Skoliose.SKOLIOSE_JA);

        // ACT
        UntersuchungBewegungsapparatSaveJsonSpec saveJsonSpec =
            UntersuchungBewegungsapparatBuilder.createJaxSpec(builder -> builder
                .forUntersuchungBewegungsapparat(existingBewegungsapparat)
                .withBemerkung("Test updateUntersuchungBewegungsapparat")
                .withSkoliose(Skoliose.SKOLIOSE_NEIN, null)).getData();
        UntersuchungBewegungsapparatJsonSpec result = api
            .untersuchungBewegungsapparatResourceSaveUntersuchungBewegungsapparat()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungBewegungsapparat");
        assertThat(result.getData().getSkoliose().getValue()).contains(SkolioseSpec.SKOLIOSE_NEIN.name());
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungBewegungsapparat() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();

        // ACT
        UntersuchungBewegungsapparatSaveJsonSpec result = api
            .untersuchungBewegungsapparatResourceGetDefaultUntersuchungBewegungsapparat()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getSkoliose()).isNotNull();
        assertThat(result.getSkoliose().getValue()).contains(Skoliose.SKOLIOSE_NEIN.name());
        assertThat(result.getHaltungsschwaeche()).isNotNull();
        assertThat(result.getHaltungsschwaeche().getValue()).contains(Haltungsschwaeche.NORMAL.name());
        assertThat(result.getVerkuerzungHamstringmuskulatur()).isNotNull();
        assertThat(result.getVerkuerzungHamstringmuskulatur().getValue())
            .contains(VerkuerzungHamstringmuskulatur.NORMAL.name());
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungBewegungsapparat_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(arzt);
        Untersuchung untersuchung = Untersuchung.forAnonymerEintrag(anonymerEintrag);
        assertThat(untersuchung.getUntersuchungBewegungsapparat()).isNull();

        // ACT
        UntersuchungBewegungsapparatSaveJsonSpec saveJsonSpec = UntersuchungBewegungsapparatBuilder.createSaveJaxSpec(
            builder -> builder
                .withSkoliose(Skoliose.SKOLIOSE_NEIN, null)
                .withHaltungsschwache(Haltungsschwaeche.PATHOLOGISCH, null)
                .withVerkuerzungHamstringmuskulatur(VerkuerzungHamstringmuskulatur.NORMAL, null)
                .withBemerkung("Test updateUntersuchungBewegungsapparat")
        );

        UntersuchungBewegungsapparatJsonSpec result = api
            .untersuchungBewegungsapparatResourceSaveUntersuchungBewegungsapparat()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungBewegungsapparat");
        assertThat(result.getData().getSkoliose().getValue()).contains(SkolioseSpec.SKOLIOSE_NEIN.name());
    }

}
