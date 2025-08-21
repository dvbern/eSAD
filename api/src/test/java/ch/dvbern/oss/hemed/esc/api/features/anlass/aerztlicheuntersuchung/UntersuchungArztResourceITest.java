package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.util.Collections;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfang;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungArztApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BewegungsapparatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BlutdruckSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinbeinhuepfenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinbeinstandSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FingeroppositionSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HautSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HerzauskultationSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.MundAndZaehneSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SprachfaehigkeitSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerhaltenSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungArztResourceITest extends AbstractITest {

    private final UntersuchungArztApiSpec api = UntersuchungArztApiSpec.untersuchungArzt(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungArztService untersuchungArztService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void getUntersuchungArzt_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungArztResourceGetUntersuchungArzt()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungArzt_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchung();
        Assertions.assertThat(untersuchung.getUntersuchungArzt()).isNotNull();

        UntersuchungArzt existingArzt =
            untersuchungArztService.getById(untersuchung.getUntersuchungArzt().getId());
        Assertions.assertThat(existingArzt).isNotNull();
        Assertions.assertThat(existingArzt.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungArztJsonSpec result = api
            .untersuchungArztResourceGetUntersuchungArzt()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingArzt.getId().getId());
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingArzt.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungArzt_should_work_for_update() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchung();
        Assertions.assertThat(untersuchung.getUntersuchungArzt()).isNotNull();
        UntersuchungArzt existingArztuntersuchung =
            untersuchungArztService.getById(untersuchung.getUntersuchungArzt().getId());
        Assertions.assertThat(existingArztuntersuchung).isNotNull();
        Assertions.assertThat(existingArztuntersuchung.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung Arzt Bemerkung");

        // ACT
        // Create a new UntersuchungArztSaveJsonSpec
        UntersuchungArztSaveJsonSpec saveJsonSpec =
            UntersuchungArztBuilder.createJaxSpec(builder -> builder
                .forUntersuchungArzt(existingArztuntersuchung)
                .withBlutdruck(Blutdruck.NORMAL)
                .withBewegungsapparat(Collections.singletonList(Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withSprachfaehigkeit(List.of(
                    Sprachfaehigkeit.ARTIKULATIONSFEHLER,
                    Sprachfaehigkeit.ENTWICKLUNGSVERZOEGERUNG))
                .withHerzauskultation(Herzauskultation.UNAUFFAELLIG)
                .withBemerkungen("Testbemerkung Untersuchung Arzt Update")).getData();

        UntersuchungArztJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArzt()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Testbemerkung Untersuchung Arzt Update");
        Assertions.assertThat(result.getData().getBlutdruck()).isEqualTo(BlutdruckSpec.NORMAL);
        Assertions.assertThat(result.getData().getBewegungsapparat()).contains(BewegungsapparatSpec.FUSSFEHLSTELLUNG);
        Assertions.assertThat(result.getData().getSprachfaehigkeit()).contains(
            SprachfaehigkeitSpec.ARTIKULATIONSFEHLER, SprachfaehigkeitSpec.ENTWICKLUNGSVERZOEGERUNG);
        Assertions.assertThat(result.getData().getHerzauskultation()).isEqualTo(HerzauskultationSpec.UNAUFFAELLIG);
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungAsEmpfang_should_work_for_create(){
        // ARRANGE
        Einladung someEinladung = fixtureService.createEinladung();
        var us = UntersuchungArztBuilder.createJaxSpec(a -> a.withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT));

        // ACT
        UntersuchungArztJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArzt()
            .anlassIdPath(someEinladung.getAnlass().getId().getId())
            .einladungIdPath(someEinladung.getId().getId())
            .body(us.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getEinbeinstand()).isEqualTo(EinbeinstandSpec.NICHT_GEPRUEFT);

    }

    @Test
    @TestAsEmpfang
    void saveUntersuchungAsEmpfang_should_fail_for_empfang(){
        // ARRANGE
        Einladung someEinladung = fixtureService.createEinladung();
        var us = UntersuchungArztBuilder.createJaxSpec(a -> a.withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT));

        // ACT
        api
            .untersuchungArztResourceSaveUntersuchungArzt()
            .anlassIdPath(someEinladung.getAnlass().getId().getId())
            .einladungIdPath(someEinladung.getId().getId())
            .body(us.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.FORBIDDEN.getStatusCode());

    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungArztForAnlassConfigKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungArztSaveJsonSpec result = api.untersuchungArztResourceGetDefaultUntersuchungArztForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungArztSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getVerhalten()).isEqualTo(VerhaltenSpec.UNAUFFAELLIG);
        assertThat(result.getBlutdruck()).isNull();
        assertThat(result.getBlutdruckSystolisch()).isNull();
        assertThat(result.getBlutdruckDiastolisch()).isNull();
        assertThat(result.getHerzauskultation()).isEqualTo(HerzauskultationSpec.UNAUFFAELLIG);
        assertThat(result.getHerzauskultationBemerkung()).isNull();
        assertThat(result.getHaut()).isEqualTo(HautSpec.GROBKURSORISCH_UNAUFFAELLIG);
        assertThat(result.getHautBemerkung()).isNull();
        assertThat(result.getMundAndZaehne()).containsExactly(MundAndZaehneSpec.UNAUFFAELLIG);
        assertThat(result.getMundAndZaehneBemerkung()).isNull();
        assertThat(result.getSprachfaehigkeit()).containsExactly(SprachfaehigkeitSpec.GROBKURSORISCH_NORMAL);
        assertThat(result.getSprachfaehigkeitBemerkung()).isNull();
        assertThat(result.getMotorik()).isNull();
        assertThat(result.getBewegungsapparat()).containsExactly(BewegungsapparatSpec.GROBKURSORISCH_NORMAL);
        assertThat(result.getBewegungsapparatBemerkung()).isNull();
        assertThat(result.getEinbeinstand()).isEqualTo(EinbeinstandSpec.SICHER);
        assertThat(result.getEinbeinstandBemerkung()).isNull();
        assertThat(result.getEinbeinhuepfen()).isEqualTo(EinbeinhuepfenSpec.SICHER);
        assertThat(result.getEinbeinhuepfenBemerkung()).isNull();
        assertThat(result.getFingeropposition()).containsExactly(FingeroppositionSpec.ALTERSENTSPRECHEND);
        assertThat(result.getFingeroppositionBemerkung()).isNull();
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungArztForAnlassConfigNichtKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT / ASSERT
        api.untersuchungArztResourceGetDefaultUntersuchungArztForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }
}
