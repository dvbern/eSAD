package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen;

import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungVorabklaerungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungVorabklaerungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungVorabklaerungenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungVorabklaerungenSaveJsonSpec;
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
class UntersuchungVorabklaerungenResourceITest extends AbstractITest {

    private final UntersuchungVorabklaerungenApiSpec api =
        UntersuchungVorabklaerungenApiSpec.untersuchungVorabklaerungen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungVorabklaerungenService untersuchungVorabklaerungenService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungVorabklaerungen_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(HasUntersuchungIDWrapper.fromEinladungId(einladung.getId()));
        // ACT / ASSERT
        api.untersuchungVorabklaerungenResourceGetUntersuchungVorabklaerungen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungVorabklaerungen_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithVorabklaerungen();
        assertThat(untersuchung.getUntersuchungVorabklaerungen()).isNotNull();

        UntersuchungVorabklaerungen
            existingVorabklaerungen =
            untersuchungVorabklaerungenService.getById(untersuchung.getUntersuchungVorabklaerungen().getId());
        assertThat(existingVorabklaerungen).isNotNull();
        assertThat(existingVorabklaerungen.getFragebogenStatus().getEnumValue(FragebogenStatus.class))
            .isNotNull()
            .isEqualTo(FragebogenStatus.ERHALTEN_VOLLSTAENDIG);

        // ACT
        UntersuchungVorabklaerungenJsonSpec result =
            api.untersuchungVorabklaerungenResourceGetUntersuchungVorabklaerungen()
                .anlassIdPath(untersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungVorabklaerungenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingVorabklaerungen.getId().getId());
        assertThat(result.getData().getFragebogenStatus()).isNotNull();
        assertThat(result.getData()
            .getFragebogenStatus()
            .getValue()).contains(FragebogenStatus.ERHALTEN_VOLLSTAENDIG.name());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungVorabklaerungen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithVorabklaerungen();
        assertThat(untersuchung.getUntersuchungVorabklaerungen()).isNotNull();
        UntersuchungVorabklaerungen existingVorabklaerungen =
            untersuchungVorabklaerungenService.getById(untersuchung.getUntersuchungVorabklaerungen().getId());
        assertThat(existingVorabklaerungen).isNotNull();
        assertThat(existingVorabklaerungen.getFragebogenStatus().getEnumValue(FragebogenStatus.class)).isEqualTo(
            FragebogenStatus.ERHALTEN_VOLLSTAENDIG);

        // ACT
        UntersuchungVorabklaerungenSaveJsonSpec saveJsonSpec =
            UntersuchungVorabklaerungenBuilder.createJaxSpec(builder -> builder
                .forUntersuchungVorabklaerungen(existingVorabklaerungen)
                .withFragebogenStatus(FragebogenStatus.NICHT_ERHALTEN, null)).getData();

        UntersuchungVorabklaerungenJsonSpec result =
            api.untersuchungVorabklaerungenResourceSaveUntersuchungVorabklaerungen()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .body(saveJsonSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungVorabklaerungenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getFragebogenStatus()).isNotNull();
        assertThat(result.getData().getFragebogenStatus().getValue()).contains(FragebogenStatus.NICHT_ERHALTEN.name());
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungVorabklaerungen_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungVorabklaerungen()).isNull();

        // ACT
        UntersuchungVorabklaerungenSaveJsonSpec saveJsonSpec =
            UntersuchungVorabklaerungenBuilder.createSaveJaxSpec(builder -> builder
                .withFragebogenStatus(FragebogenStatus.NICHT_ERHALTEN, null)
                .withChronischeKrankheiten(ChronischeKrankheiten.NICHT_ABGEKLAERT, null)
                .withEinverstaendnisImpfungen(EinverstaendnisImpfungen.EINVERSTANDEN, null));

        UntersuchungVorabklaerungenJsonSpec result =
            api.untersuchungVorabklaerungenResourceSaveUntersuchungVorabklaerungen()
                .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
                .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
                .body(saveJsonSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungVorabklaerungenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getFragebogenStatus()).isNotNull();
        assertThat(result.getData().getFragebogenStatus().getValue()).contains(FragebogenStatus.NICHT_ERHALTEN.name());
        assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungVorabklaerungen()).isNotNull();
    }
}

