package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSehenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DistanzSehenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FarbsehenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InspektionKonvergenzResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.MotilitaetResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StereotestResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusAuftretenResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusAuspraegungResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StrabismusResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerwendetTafelResultatSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungSehenResourceITest extends AbstractITest {

    private final UntersuchungSehenApiSpec api =
        UntersuchungSehenApiSpec.untersuchungSehen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungSehenService untersuchungSehenService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehen_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungSehenResourceGetUntersuchungSehen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehen_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehen();
        assertThat(untersuchung.getUntersuchungSehen()).isNotNull();

        UntersuchungSehen existingSehen = untersuchungSehenService.getById(untersuchung.getUntersuchungSehen().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSehenJsonSpec result = api.untersuchungSehenResourceGetUntersuchungSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSehen.getId().getId());
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo(existingSehen.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehen_should_return_existing_massnahme() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        assertThat(untersuchung.getUntersuchungSehen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();

        UntersuchungSehen existingSehen = untersuchungSehenService.getById(untersuchung.getUntersuchungSehen().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull();

        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        assertThat(existingMassnahmen).isNotNull();
        assertThat(existingMassnahmen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSehenJsonSpec result = api.untersuchungSehenResourceGetUntersuchungSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSehen.getId().getId());
        assertThat(result.getData().getMassnahmen()).isNotNull();
        assertThat(result.getData().getMassnahmen().getKeineMassnahmen()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSehen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        assertThat(untersuchung.getUntersuchungSehen()).isNotNull();
        UntersuchungSehen existingSehen = untersuchungSehenService.getById(untersuchung.getUntersuchungSehen().getId());
        assertThat(existingSehen).isNotNull();
        assertThat(existingSehen.getBemerkungen()).isNotNull().isEqualTo("Untersuchung Sehen Bemerkung");

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec sehenMassnahmenSaveJax =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withKeineMassnahmen(true));
        UntersuchungSehenSaveJsonSpec saveJsonSpec =
            UntersuchungSehenBuilder.createJaxSpec(builder -> builder.forUntersuchungSehen(existingSehen)
                .withStereotest(StereotestResultat.UNSICHER)
                .withBemerkung("Test updateUntersuchungSehen")).getData();
        saveJsonSpec.setMassnahmen(sehenMassnahmenSaveJax);

        UntersuchungSehenJsonSpec result = api.untersuchungSehenResourceSaveUntersuchungSehen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo("Test updateUntersuchungSehen");
        assertThat(result.getData().getStereotest()).isEqualTo(StereotestResultatSpec.UNSICHER);
        assertThat(result.getData().getMassnahmen()).isNotNull();
        assertThat(result.getData().getMassnahmen().getKeineMassnahmen()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenForAnlassConfig() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNull();
        assertThat(result.getStereotest()).isEqualTo(StereotestResultatSpec.NORMAL);
        assertThat(result.getFarbsehen()).isEqualTo(FarbsehenResultatSpec.NORMAL);
        assertThat(result.getVerwendeteTafel()).isEqualTo(VerwendetTafelResultatSpec.C_TAFEL);
        assertThat(result.getFernvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NORMAL));
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getNahvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NICHT_GEPRUEFT));
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isEqualTo(MotilitaetResultatSpec.NORMAL);
        assertThat(result.getStrabismus()).isEqualTo(List.of(StrabismusResultatSpec.KEIN_STRABISMUS));
        assertThat(result.getStrabismusAuftreten()).isEqualTo(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getStrabismusAuspraegung()).isEqualTo(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getInspektion()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getKonvergenz()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getBemerkungen()).isNull();
        assertThat(result.getMassnahmen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenForAnlassConfigKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNull();
        assertThat(result.getStereotest()).isEqualTo(StereotestResultatSpec.NORMAL);
        assertThat(result.getFarbsehen()).isEqualTo(FarbsehenResultatSpec.NORMAL);
        assertThat(result.getVerwendeteTafel()).isEqualTo(VerwendetTafelResultatSpec.E_TAFEL);
        assertThat(result.getFernvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NORMAL));
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(0.8));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(0.8));
        assertThat(result.getNahvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NICHT_GEPRUEFT));
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isEqualTo(MotilitaetResultatSpec.NORMAL);
        assertThat(result.getStrabismus()).isEqualTo(List.of(StrabismusResultatSpec.KEIN_STRABISMUS));
        assertThat(result.getStrabismusAuftreten()).isEqualTo(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getStrabismusAuspraegung()).isEqualTo(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getInspektion()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getKonvergenz()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getBemerkungen()).isNull();
        assertThat(result.getMassnahmen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenForAnlassConfigFremdsprachen() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.FREMDSPRACHEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNull();
        assertThat(result.getStereotest()).isEqualTo(StereotestResultatSpec.NICHT_GEPRUEFT);
        assertThat(result.getFarbsehen()).isEqualTo(FarbsehenResultatSpec.NORMAL);
        assertThat(result.getVerwendeteTafel()).isEqualTo(VerwendetTafelResultatSpec.C_TAFEL);
        assertThat(result.getFernvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NORMAL));
        assertThat(result.getFernvisusRechts()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getFernvisusLinks()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(result.getNahvisus()).isEqualTo(List.of(DistanzSehenResultatSpec.NICHT_GEPRUEFT));
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isEqualTo(MotilitaetResultatSpec.NORMAL);
        assertThat(result.getStrabismus()).isEqualTo(List.of(StrabismusResultatSpec.KEIN_STRABISMUS));
        assertThat(result.getStrabismusAuftreten()).isEqualTo(StrabismusAuftretenResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getStrabismusAuspraegung()).isEqualTo(StrabismusAuspraegungResultatSpec.KEIN_STRABISMUS);
        assertThat(result.getInspektion()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getKonvergenz()).isEqualTo(InspektionKonvergenzResultatSpec.UNAUFFAELLIG);
        assertThat(result.getBemerkungen()).isNull();
        assertThat(result.getMassnahmen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungSehenForAnlassConfig_NOT_VISIBLE() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithSameRequirementSettings(RequirementSetting.NOT_VISIBLE);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungSehenSaveJsonSpec result = api.untersuchungSehenResourceGetDefaultUntersuchungSehenForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getBrilleLinsen()).isNull();
        assertThat(result.getStereotest()).isNull();
        assertThat(result.getFarbsehen()).isNull();
        assertThat(result.getVerwendeteTafel()).isNull();
        assertThat(result.getFernvisus()).isNull();
        assertThat(result.getFernvisusRechts()).isNull();
        assertThat(result.getFernvisusLinks()).isNull();
        assertThat(result.getNahvisus()).isNull();
        assertThat(result.getNahvisusRechts()).isNull();
        assertThat(result.getNahvisusLinks()).isNull();
        assertThat(result.getMotilitaet()).isNull();
        assertThat(result.getStrabismus()).isNull();
        assertThat(result.getStrabismusAuftreten()).isNull();
        assertThat(result.getStrabismusAuspraegung()).isNull();
        assertThat(result.getInspektion()).isNull();
        assertThat(result.getKonvergenz()).isNull();
        assertThat(result.getBemerkungen()).isNull();
        assertThat(result.getMassnahmen()).isNull();
    }
}
