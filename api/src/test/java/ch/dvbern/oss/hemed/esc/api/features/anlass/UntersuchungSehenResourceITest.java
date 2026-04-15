package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBemerkungFragebogenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSehenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BrilleLinsenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DistanzSehenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FarbsehenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InspektionKonvergenzResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.MotilitaetResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StereotestResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusAuftretenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusAuspraegungResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBemerkungFragebogenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBemerkungFragebogenSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerwendetTafelResultatSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungSehenResourceITest extends AbstractITest {

    private final UntersuchungSehenApiSpec api =
        UntersuchungSehenApiSpec.untersuchungSehen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungSehenBeurteilungService untersuchungSehenBeurteilungService;
    private final UntersuchungSehenBemerkungFragebogenService untersuchungSehenBemerkungFragebogenService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final FixtureService fixtureService;
    private final TenantProducer tenantProducer;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenBeurteilung_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungSehenResourceGetUntersuchungSehenBeurteilung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenBeurteilung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehen();
        assertThat(untersuchung.getUntersuchungSehenBeurteilung()).isNotNull();

        UntersuchungSehenBeurteilung
                existingSehen = untersuchungSehenBeurteilungService.getById(untersuchung.getUntersuchungSehenBeurteilung().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSehenBeurteilungJsonSpec result = api.untersuchungSehenResourceGetUntersuchungSehenBeurteilung()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSehen.getId().getId());
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo(existingSehen.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenBeurteilung_should_return_existing_massnahme() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        assertThat(untersuchung.getUntersuchungSehenBeurteilung()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();

        UntersuchungSehenBeurteilung
                existingSehen = untersuchungSehenBeurteilungService.getById(untersuchung.getUntersuchungSehenBeurteilung().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull();

        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        assertThat(existingMassnahmen).isNotNull();
        assertThat(existingMassnahmen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSehenBeurteilungJsonSpec result = api.untersuchungSehenResourceGetUntersuchungSehenBeurteilung()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSehen.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSehenBeurteilung() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        assertThat(untersuchung.getUntersuchungSehenBeurteilung()).isNotNull();
        UntersuchungSehenBeurteilung
                existingSehen = untersuchungSehenBeurteilungService.getById(untersuchung.getUntersuchungSehenBeurteilung().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull().isEqualTo("Untersuchung Sehen Bemerkung");

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec saveJsonSpec =
            UntersuchungSehenBeurteilungBuilder.createJaxSpec(builder -> builder.forUntersuchungSehen(existingSehen)
                .withStereotest(StereotestResultat.UNSICHER, null)
                .withBemerkung("Test updateUntersuchungSehen")).getData();

        UntersuchungSehenBeurteilungJsonSpec result = api.untersuchungSehenResourceSaveUntersuchungSehenBeurteilung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo("Test updateUntersuchungSehen");
        assertThat(result.getData().getStereotest()).isNotNull();
        assertThat(result.getData().getStereotest().getValue()).contains(StereotestResultatSpec.UNSICHER.name());
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenBeurteilungForAnlassConfig() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNotNull();
        assertThat(result.getBrilleLinsen().getValue()).isNull();
        assertThat(result.getStereotest()).isNotNull();
        assertThat(result.getStereotest().getValue()).contains(StereotestResultatSpec.NORMAL.name());
        assertThat(result.getFarbsehen()).isNotNull();
        assertThat(result.getFarbsehen().getValue()).contains(FarbsehenResultatSpec.NORMAL.name());
        assertThat(result.getVerwendeteTafel()).isNotNull();
        assertThat(result.getVerwendeteTafel().getValue()).contains(VerwendetTafelResultatSpec.C_TAFEL.name());
        assertThat(result.getFernvisus().getValue()).isEqualTo(List.of(DistanzSehenResultatSpec.NORMAL.name()));
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getNahvisus().getValue()).isEqualTo(List.of(DistanzSehenResultatSpec.NICHT_GEPRUEFT.name()));
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isNotNull();
        assertThat(result.getMotilitaet().getValue()).contains(MotilitaetResultatSpec.NORMAL.name());
        assertThat(result.getStrabismus().getValue()).isEqualTo(List.of(StrabismusResultatSpec.KEIN_STRABISMUS.name()));
        assertThat(result.getStrabismusAuftreten()).isNotNull();
        assertThat(result.getStrabismusAuftreten().getValue()).contains(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getStrabismusAuspraegung()).isNotNull();
        assertThat(result.getStrabismusAuspraegung().getValue()).contains(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getInspektion()).isNotNull();
        assertThat(result.getInspektion().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getKonvergenz()).isNotNull();
        assertThat(result.getKonvergenz().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenBeurteilungForAnlassConfigKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungSaveJsonSpec.class);

        // ASSERT
        // Special case for BS Brillen value is null
        assertThat(result.getBrilleLinsen().getValue()).isNull();

        this.assertCommonDefaultValuesSehenBeurteilungKG(result);
    }

    @Test
    @TestAsAdminBE
    void getDefaultUntersuchungSehenBeurteilungForAnlassConfigKindergartenWithTenantNotBsSetsBrilleToOhneKeine() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungSaveJsonSpec.class);

        // ASSERT
        // Normal case for WIN Brillen value is provided
        assertThat(result.getBrilleLinsen()).isNotNull();
        assertThat(result.getBrilleLinsen().getValue()).contains(BrilleLinsenResultatSpec.OHNE_KEINE.name());

        this.assertCommonDefaultValuesSehenBeurteilungKG(result);
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenBeurteilungForAnlassConfigFremdsprachen() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.FREMDSPRACHEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNotNull();
        assertThat(result.getBrilleLinsen().getValue()).isNull();
        assertThat(result.getStereotest()).isNotNull();
        assertThat(result.getStereotest().getValue()).contains(StereotestResultatSpec.NICHT_GEPRUEFT.name());
        assertThat(result.getFarbsehen()).isNotNull();
        assertThat(result.getFarbsehen().getValue()).contains(FarbsehenResultatSpec.NORMAL.name());
        assertThat(result.getVerwendeteTafel()).isNotNull();
        assertThat(result.getVerwendeteTafel().getValue()).contains(VerwendetTafelResultatSpec.C_TAFEL.name());
        assertThat(result.getFernvisus().getValue()).isEqualTo(List.of(DistanzSehenResultatSpec.NORMAL.name()));
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getNahvisus().getValue()).isEqualTo(List.of(DistanzSehenResultatSpec.NICHT_GEPRUEFT.name()));
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isNotNull();
        assertThat(result.getMotilitaet().getValue()).contains(MotilitaetResultatSpec.NORMAL.name());
        assertThat(result.getStrabismus().getValue()).isEqualTo(List.of(StrabismusResultatSpec.KEIN_STRABISMUS.name()));
        assertThat(result.getStrabismusAuftreten()).isNotNull();
        assertThat(result.getStrabismusAuftreten().getValue()).contains(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getStrabismusAuspraegung()).isNotNull();
        assertThat(result.getStrabismusAuspraegung().getValue()).contains(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getInspektion()).isNotNull();
        assertThat(result.getInspektion().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getKonvergenz()).isNotNull();
        assertThat(result.getKonvergenz().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenBeurteilungForAnlassConfig_NOT_VISIBLE() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithSameRequirementSettings(RequirementSetting.NOT_VISIBLE);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenBeurteilungSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBeurteilungSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen().getValue()).isNull();
        assertThat(result.getStereotest().getValue()).isNull();
        assertThat(result.getFarbsehen().getValue()).isNull();
        assertThat(result.getVerwendeteTafel().getValue()).isNull();
        assertThat(result.getFernvisus().getValue()).isEmpty();
        assertThat(result.getFernvisusRechts()).isNull();
        assertThat(result.getFernvisusLinks()).isNull();
        assertThat(result.getNahvisus().getValue()).isEmpty();
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet().getValue()).isNull();
        assertThat(result.getStrabismus().getValue()).isEmpty();
        assertThat(result.getStrabismusAuftreten().getValue()).isNull();
        assertThat(result.getStrabismusAuspraegung().getValue()).isNull();
        assertThat(result.getInspektion().getValue()).isNull();
        assertThat(result.getKonvergenz().getValue()).isNull();
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenBemerkungFragebogen_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungSehenResourceGetUntersuchungSehenBemerkungFragebogen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenBemerkungFragebogen_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehen();
        assertThat(untersuchung.getUntersuchungSehenBemerkungFragebogen()).isNotNull();

        UntersuchungSehenBemerkungFragebogen
            existingSehen = untersuchungSehenBemerkungFragebogenService.getById(untersuchung.getUntersuchungSehenBemerkungFragebogen().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungenFragebogen()).isNotNull();
        // ACT
        UntersuchungSehenBemerkungFragebogenJsonSpec result = api.untersuchungSehenResourceGetUntersuchungSehenBemerkungFragebogen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBemerkungFragebogenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSehen.getId().getId());
        assertThat(result.getData().getBemerkungenFragebogen()).isNotNull().isEqualTo(existingSehen.getBemerkungenFragebogen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSehenBemerkungFragebogen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehen();
        assertThat(untersuchung.getUntersuchungSehenBemerkungFragebogen()).isNotNull();
        UntersuchungSehenBemerkungFragebogen
            existingSehen = untersuchungSehenBemerkungFragebogenService.getById(untersuchung.getUntersuchungSehenBemerkungFragebogen().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungenFragebogen()).isNotNull().isEqualTo("Untersuchung Sehen Fragebogen Bemerkung");

        // ACT
        UntersuchungSehenBemerkungFragebogenSaveJsonSpec saveJsonSpec =
            UntersuchungSehenBemerkungFragebogenBuilder.createJaxSpec(builder -> builder.forUntersuchungSehenBemerkungFragebogen(existingSehen)
                .withBemerkungFragebogen("Test updateUntersuchungSehenBemerkungFragebogen")).getData();

        UntersuchungSehenBemerkungFragebogenJsonSpec result = api.untersuchungSehenResourceSaveUntersuchungSehenBemerkungFragebogen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenBemerkungFragebogenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungenFragebogen()).isNotNull().isEqualTo("Test updateUntersuchungSehenBemerkungFragebogen");
    }

    private void assertCommonDefaultValuesSehenBeurteilungKG(@NonNull UntersuchungSehenBeurteilungSaveJsonSpec result){
        assertThat(result.getStereotest()).isNotNull();
        assertThat(result.getStereotest().getValue()).contains(StereotestResultatSpec.NORMAL.name());
        assertThat(result.getFarbsehen()).isNotNull();
        assertThat(result.getFarbsehen().getValue()).contains(FarbsehenResultatSpec.NORMAL.name());
        assertThat(result.getVerwendeteTafel()).isNotNull();
        assertThat(result.getVerwendeteTafel().getValue()).contains(VerwendetTafelResultatSpec.E_TAFEL.name());
        assertThatList(result.getFernvisus().getValue()).containsExactlyInAnyOrder(DistanzSehenResultatSpec.NORMAL.name());
        assertThatList(result.getNahvisus().getValue()).containsExactlyInAnyOrder(DistanzSehenResultatSpec.NICHT_GEPRUEFT.name());
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isNotNull();
        assertThat(result.getMotilitaet().getValue()).contains(MotilitaetResultatSpec.NORMAL.name());
        assertThatList(result.getStrabismus().getValue()).containsExactlyInAnyOrder(StrabismusResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getStrabismusAuftreten()).isNotNull();
        assertThat(result.getStrabismusAuftreten().getValue()).contains(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getStrabismusAuspraegung()).isNotNull();
        assertThat(result.getStrabismusAuspraegung().getValue()).contains(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS.name());
        assertThat(result.getInspektion()).isNotNull();
        assertThat(result.getInspektion().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getKonvergenz()).isNotNull();
        assertThat(result.getKonvergenz().getValue()).contains(InspektionKonvergenzResultatSpec.UNAUFFAELLIG.name());
        assertThat(result.getBemerkungen()).isNull();
    }
}
