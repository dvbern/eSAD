package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungAbschlussBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungAbschlussApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungAbschlussJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungAbschlussSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungAbschlussResourceITest extends AbstractITest {

    private final UntersuchungAbschlussApiSpec api = UntersuchungAbschlussApiSpec.untersuchungAbschluss(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungAbschlussService untersuchungAbschlussService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungAbschluss_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungAbschlussResourceGetUntersuchungAbschluss()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungAbschluss_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithAbschluss();
        assertThat(untersuchung.getUntersuchungAbschluss()).isNotNull();

        UntersuchungAbschluss
            existingAbschluss = untersuchungAbschlussService.getById(untersuchung.getUntersuchungAbschluss().getId());
        assertThat(existingAbschluss).isNotNull();
        assertThat(existingAbschluss.getUeberweisungErfolgt().getEnumValue(AbschlussResultat.class)).isEqualTo(AbschlussResultat.JA);
        assertThat(existingAbschluss.getBeurteilungEmpfohleneMassnahmen()).isNotNull();
        // ACT
        UntersuchungAbschlussJsonSpec result = api.untersuchungAbschlussResourceGetUntersuchungAbschluss()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungAbschlussJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingAbschluss.getId().getId());
        assertThat(existingAbschluss.getUeberweisungErfolgt()).isNotNull();
        String existingUeberweisungErfolg = existingAbschluss.getUeberweisungErfolgt().toEnumValueName(AbschlussResultat.class);
        assertThat(existingUeberweisungErfolg).isNotNull();
        assertThat(result.getAbschlussData().getUeberweisungErfolgt()).isNotNull();
        assertThat(result.getAbschlussData().getUeberweisungErfolgt().getValue()).contains(existingUeberweisungErfolg);
        assertThat(result.getAbschlussData().getBeurteilungEmpfohleneMassnahmen()).isNotNull().isEqualTo(existingAbschluss.getBeurteilungEmpfohleneMassnahmen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungAbschluss() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithAbschluss();
        assertThat(untersuchung.getUntersuchungAbschluss()).isNotNull();
        UntersuchungAbschluss
            existingAbschluss = untersuchungAbschlussService.getById(untersuchung.getUntersuchungAbschluss().getId());
        assertThat(existingAbschluss).isNotNull();
        assertThat(existingAbschluss.getUeberweisungErfolgt().getEnumValue(AbschlussResultat.class)).isEqualTo(AbschlussResultat.JA);
        assertThat(existingAbschluss.getBeurteilungEmpfohleneMassnahmen()).isNotNull().isEqualTo("Empfohlene Massnahme");

        // ACT
        UntersuchungAbschlussSaveJsonSpec saveJsonSpec =
            UntersuchungAbschlussBuilder.createJaxSpec(builder -> builder
                .forUntersuchungAbschluss(existingAbschluss)
                .withUeberweisungErfolgt(AbschlussResultat.NEIN, null)
                .withBeurteilungEmpfohleneMassnahmen("Test updateUntersuchungAbschluss")).getAbschlussData();

        UntersuchungAbschlussJsonSpec result = api.untersuchungAbschlussResourceSaveUntersuchungAbschluss()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungAbschlussJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAbschlussData().getBeurteilungEmpfohleneMassnahmen()).isNotNull().isEqualTo("Test updateUntersuchungAbschluss");
        assertThat(result.getAbschlussData().getUeberweisungErfolgt()).isNotNull();
        assertThat(result.getAbschlussData().getUeberweisungErfolgt().getValue()).contains(AbschlussResultat.NEIN.name());
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungAbschluss_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        // Create anlass with arzt set (to pass authorizer)
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung).isNotNull();

        // ACT
        UntersuchungAbschlussSaveJsonSpec saveJsonSpec =
            UntersuchungAbschlussBuilder.createJaxSpec(builder -> builder
                .withUeberweisungErfolgt(AbschlussResultat.NEIN, null)
                .withBeratungDurchgefuehrt(AbschlussResultat.NEIN, null)
                .withAerztlicheValidierungErforderlich(AbschlussResultat.NEIN, null)
                .withBeurteilungEmpfohleneMassnahmen("Test saveUntersuchungAbschluss anonym")).getAbschlussData();

        var response = api.untersuchungAbschlussResourceSaveUntersuchungAbschluss()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek);

        UntersuchungAbschlussJsonSpec result = response
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungAbschlussJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAbschlussData().getBeurteilungEmpfohleneMassnahmen())
            .isNotNull()
            .isEqualTo("Test saveUntersuchungAbschluss anonym");
        assertThat(result.getAbschlussData().getUeberweisungErfolgt()).isNotNull();
        assertThat(result.getAbschlussData().getUeberweisungErfolgt().getValue()).contains(AbschlussResultat.NEIN.name());
    }

}
