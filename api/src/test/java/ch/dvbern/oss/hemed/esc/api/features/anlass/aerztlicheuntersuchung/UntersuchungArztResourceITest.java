package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBemerkungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfangBS;
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
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerhaltenSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungArztResourceITest extends AbstractITest {

    private final UntersuchungArztApiSpec api = UntersuchungArztApiSpec.untersuchungArzt(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungArztBeurteilungService untersuchungArztBeurteilungService;
    private final UntersuchungArztBemerkungService untersuchungArztBemerkungService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void getUntersuchungArzt_Beurteilung_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungArztResourceGetUntersuchungArztBeurteilung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungArzt_Beurteilung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchung();
        Assertions.assertThat(untersuchung.getUntersuchungArztBeurteilung()).isNotNull();

        UntersuchungArztBeurteilung existingArzt =
            untersuchungArztBeurteilungService.getById(untersuchung.getUntersuchungArztBeurteilung().getId());
        Assertions.assertThat(existingArzt).isNotNull();
        // ACT
        UntersuchungArztBeurteilungJsonSpec result = api
            .untersuchungArztResourceGetUntersuchungArztBeurteilung()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBeurteilungJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingArzt.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungArzt_Beurteilung_should_work_for_update() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchung();
        Assertions.assertThat(untersuchung.getUntersuchungArztBeurteilung()).isNotNull();
        UntersuchungArztBeurteilung existingArztuntersuchung =
            untersuchungArztBeurteilungService.getById(untersuchung.getUntersuchungArztBeurteilung().getId());
        Assertions.assertThat(existingArztuntersuchung).isNotNull();
        BigDecimal motorikPunkte = new BigDecimal("10.5");

        // ACT
        // Create a new UntersuchungArztBeurteilungSaveJsonSpec
        UntersuchungArztBeurteilungSaveJsonSpec saveJsonSpec =
            UntersuchungArztBeurteilungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungArzt(existingArztuntersuchung)
                .withBlutdruck(Blutdruck.NORMAL)
                .withBewegungsapparat(Collections.singletonList(Bewegungsapparat.FUSSFEHLSTELLUNG))
                .withSprachfaehigkeit(List.of(
                    Sprachfaehigkeit.ARTIKULATIONSFEHLER,
                    Sprachfaehigkeit.ENTWICKLUNGSVERZOEGERUNG))
                .withHerzauskultation(Herzauskultation.UNAUFFAELLIG)
                .withMotorikPunkte(motorikPunkte)).getData();

        UntersuchungArztBeurteilungJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArztBeurteilung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBeurteilungJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBlutdruck()).isEqualTo(BlutdruckSpec.NORMAL);
        Assertions.assertThat(result.getData().getBewegungsapparat()).contains(BewegungsapparatSpec.FUSSFEHLSTELLUNG);
        Assertions.assertThat(result.getData().getSprachfaehigkeitMultiValue()).contains(
            SprachfaehigkeitSpec.ARTIKULATIONSFEHLER, SprachfaehigkeitSpec.ENTWICKLUNGSVERZOEGERUNG);
        Assertions.assertThat(result.getData().getHerzauskultation()).isEqualTo(HerzauskultationSpec.UNAUFFAELLIG);
        Assertions.assertThat(result.getData().getMotorikPunkte()).isEqualTo(motorikPunkte);
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungAsEmpfang_should_work_for_create() {
        // ARRANGE
        Einladung someEinladung = fixtureService.createEinladung();
        var us = UntersuchungArztBeurteilungBuilder.createJaxSpec(a -> a.withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT));

        // ACT
        UntersuchungArztBeurteilungJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArztBeurteilung()
            .anlassIdPath(someEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(someEinladung.getId()))
            .body(us.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBeurteilungJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getEinbeinstand()).isEqualTo(EinbeinstandSpec.NICHT_GEPRUEFT);
    }

    @Test
    @TestAsEmpfangBS
    void saveUntersuchungAsEmpfang_should_fail_for_empfang() {
        // ARRANGE
        Einladung someEinladung = fixtureService.createEinladung();
        var us = UntersuchungArztBeurteilungBuilder.createJaxSpec(a -> a.withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT));

        // ACT
        api
            .untersuchungArztResourceSaveUntersuchungArztBeurteilung()
            .anlassIdPath(someEinladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(someEinladung.getId()))
            .body(us.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungArztBeurteilungForAnlassConfigKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungArztBeurteilungSaveJsonSpec result =
            api.untersuchungArztResourceGetDefaultUntersuchungArztBeurteilungForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungArztBeurteilungSaveJsonSpec.class);

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
        assertThat(result.getSprachfaehigkeitMultiValue()).containsExactly(SprachfaehigkeitSpec.GROBKURSORISCH_NORMAL);
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
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungArztBeurteilungForAnlassConfigNichtKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT / ASSERT
        api.untersuchungArztResourceGetDefaultUntersuchungArztBeurteilungForAnlassConfig()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungArztBemerkung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchungBemerkung();
        Assertions.assertThat(untersuchung.getUntersuchungArztBemerkung()).isNotNull();

        UntersuchungArztBemerkung existingBemerkung =
            untersuchungArztBemerkungService.getById(untersuchung.getUntersuchungArztBemerkung().getId());
        Assertions.assertThat(existingBemerkung).isNotNull();
        // ACT
        UntersuchungArztBemerkungJsonSpec result = api
            .untersuchungArztResourceGetUntersuchungArztBemerkung()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBemerkungJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingBemerkung.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungArztBeurteilungBemerkung_should_work_for_update() {
        // ARRANGE

        Untersuchung untersuchung = fixtureService.createUntersuchungWithArztuntersuchungBemerkung();
        Einladung einladung = untersuchung.getEinladungRequired();
        Assertions.assertThat(untersuchung.getUntersuchungArztBemerkung()).isNotNull();
        UntersuchungArztBemerkung existingBemerkung =
            untersuchungArztBemerkungService.getById(untersuchung.getUntersuchungArztBemerkung().getId());
        Assertions.assertThat(existingBemerkung).isNotNull();
        Assertions.assertThat(existingBemerkung.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung Arzt Bemerkung");

        // ACT
        // Create a new UntersuchungArztBeurteilungSaveJsonSpec
        UntersuchungArztBemerkungSaveJsonSpec saveJsonSpec =
            UntersuchungArztBemerkungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungArztBemerkung(existingBemerkung)
                .withBemerkungen("Testbemerkung Untersuchung Arzt Update")
            ).getData();

        UntersuchungArztBemerkungJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArztBemerkung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBemerkungJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Testbemerkung Untersuchung Arzt Update");
    }
}
