package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSchulrelevanteBeeintraechtigungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSchulrelevanteBeeintraechtigungApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FreiesGehenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungSaveJsonSpec;
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
class UntersuchungSchulrelevanteBeeintraechtigungResourceITest extends AbstractITest {

    private final UntersuchungSchulrelevanteBeeintraechtigungApiSpec api =
        UntersuchungSchulrelevanteBeeintraechtigungApiSpec.untersuchungSchulrelevanteBeeintraechtigung(
            RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungSchulrelevanteBeeintraechtigungService untersuchungBeeintraechtigungService;
    private final FixtureService fixtureService;
    private final TenantProducer tenantProducer;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSchulrelevanteBeeintraechtigung_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungSchulrelevanteBeeintraechtigungResourceGetUntersuchungSchulrelevanteBeeintraechtigung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSchulrelevanteBeeintraechtigung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSchulrelevanteBeeintraechtigung();
        assertThat(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()).isNotNull();

        UntersuchungSchulrelevanteBeeintraechtigung
            existingBeeintraechtigung =
            untersuchungBeeintraechtigungService.getById(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()
                .getId());
        assertThat(existingBeeintraechtigung).isNotNull();
        assertThat(existingBeeintraechtigung.getAnzahlMonateFreiesGehen()).isEqualTo(15);
        assertThat(existingBeeintraechtigung.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSchulrelevanteBeeintraechtigungJsonSpec result =
            api.untersuchungSchulrelevanteBeeintraechtigungResourceGetUntersuchungSchulrelevanteBeeintraechtigung()
                .anlassIdPath(untersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungSchulrelevanteBeeintraechtigungJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingBeeintraechtigung.getId().getId());
        assertThat(result.getData()
            .getAnzahlMonateFreiesGehen()).isEqualTo(existingBeeintraechtigung.getAnzahlMonateFreiesGehen());
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo(existingBeeintraechtigung.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSchulrelevanteBeeintraechtigung() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSchulrelevanteBeeintraechtigung();
        assertThat(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()).isNotNull();
        UntersuchungSchulrelevanteBeeintraechtigung
            existingBeeintraechtigung =
            untersuchungBeeintraechtigungService.getById(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()
                .getId());
        assertThat(existingBeeintraechtigung).isNotNull();
        assertThat(existingBeeintraechtigung.getAnzahlMonateFreiesGehen()).isEqualTo(15);
        assertThat(existingBeeintraechtigung.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung Schulrelevante Beeinträchtigung Bemerkung");

        // ACT
        UntersuchungSchulrelevanteBeeintraechtigungSaveJsonSpec saveJsonSpec =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungSchulrelevanteBeeintraechtigung(existingBeeintraechtigung)
                .withAnzahlMonateFreiesGehen(10)
                .withBemerkungen("Test updateUntersuchungSchulrelevanteBeeintraechtigung")).getData();

        UntersuchungSchulrelevanteBeeintraechtigungJsonSpec result =
            api.untersuchungSchulrelevanteBeeintraechtigungResourceSaveUntersuchungSchulrelevanteBeeintraechtigung()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .body(saveJsonSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungSchulrelevanteBeeintraechtigungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull()
            .isEqualTo("Test updateUntersuchungSchulrelevanteBeeintraechtigung");
        assertThat(result.getData().getAnzahlMonateFreiesGehen()).isEqualTo(10);
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungSchulrelevanteBeeintraechtigung_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(arzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        assertThat(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()).isNull();

        // ACT
        UntersuchungSchulrelevanteBeeintraechtigungSaveJsonSpec saveJsonSpec =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.createSaveJaxSpec(builder -> builder
                .withFreiesGehen(FreiesGehen.NICHT_GEPRUEFT, NichtGeprueftGrund.ANDERER_GRUND)
                .withOhneStuetzradVelofahren(OhneStuetzradVelofahren.JA_OHNE_STUETZRAD_VELOFAHREN, null)
                .withGrafomotorik(Grafomotorik.GUT, null)
                .withFeinmotorik(Feinmotorik.NICHT_BESTANDEN, null)
                .withGrobmotorik(GrobmotorikEinbeinhuepfen.BESTANDEN, null)
                .withGleichgewicht(GleichgewichtEinbeinstand.BESTANDEN, null)
                .withBemerkungen("Test updateUntersuchungSchulrelevanteBeeintraechtigung"));

        UntersuchungSchulrelevanteBeeintraechtigungJsonSpec result =
            api.untersuchungSchulrelevanteBeeintraechtigungResourceSaveUntersuchungSchulrelevanteBeeintraechtigung()
                .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
                .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
                .body(saveJsonSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungSchulrelevanteBeeintraechtigungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getVersion()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull()
            .isEqualTo("Test updateUntersuchungSchulrelevanteBeeintraechtigung");
        assertThat(result.getData().getFreiesGehen().getValue()).contains(FreiesGehenSpec.NICHT_GEPRUEFT.name());
        assertThat(result.getData().getOhneStuetzradVelofahren().getValue())
            .contains(OhneStuetzradVelofahren.JA_OHNE_STUETZRAD_VELOFAHREN.name());
        assertThat(result.getData().getGrafomotorik().getValue()).contains(Grafomotorik.GUT.name());
        assertThat(result.getData()
            .getFeinmotorik()
            .getValue()).contains(Feinmotorik.NICHT_BESTANDEN.name());
        assertThat(result.getData().getGrobmotorikEinbeinhuepfen().getValue())
            .contains(GrobmotorikEinbeinhuepfen.BESTANDEN.name());
        assertThat(result.getData().getGleichgewichtEinbeinstand().getValue())
            .contains(GleichgewichtEinbeinstand.BESTANDEN.name());

    }
}
