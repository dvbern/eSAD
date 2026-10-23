package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Aussprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallFangen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallWerfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.FigurNachmalen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Hampelmann;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Kognition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Lesefit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.NameSchreiben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Stifthaltung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhaltensfragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZahlenNachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Zeichnen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZeichnenMensch;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBemerkungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfangBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungArztApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AusspracheSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BallFangenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BallWerfenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BewegungsapparatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BlutdruckSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinbeinhuepfenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinbeinstandSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinverstaendniserklaerungSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FigurNachmalenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FingeroppositionSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HampelmannSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HautSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HerzauskultationSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KognitionSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LesefitSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.MotorikSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.MundAndZaehneSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NameSchreibenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SprachfaehigkeitSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.StifthaltungSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerhaltenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.VerhaltensfragebogenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ZahlenNachsprechenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ZeichnenMenschSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ZeichnenSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungArztResourceITest extends AbstractITest {

    private final UntersuchungArztApiSpec api = UntersuchungArztApiSpec.untersuchungArzt(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungArztBeurteilungService untersuchungArztBeurteilungService;
    private final UntersuchungArztBemerkungService untersuchungArztBemerkungService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

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
    void saveUntersuchungAsAdmin_should_work_for_create() {
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
    @TestAsSchularztBE
    @SuppressWarnings("java:S5961" /*Number of assertions is ok, rather more than less*/)
    void saveUntersuchungArztBeurteilung_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer benutzer = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(benutzer);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungArztBeurteilung()).isNull();

        // ACT
        UntersuchungArztBeurteilungSaveJsonSpec saveJsonSpec = UntersuchungArztBeurteilungBuilder.createSaveJaxSpec(
            builder -> builder
                .withEinverstaendniserklaerung(EnumSetUtil.toEnumSet(
                    List.of(Einverstaendniserklaerung.SCHWEIGEPFLICHT_VORHANDEN),
                    Einverstaendniserklaerung.class))
                .withVerhaltensfragebogen(Verhaltensfragebogen.VORHANDEN)
                .withVerhalten(Verhalten.AUFFAELLIG)
                .withBlutdruck(Blutdruck.NORMAL)
                .withBlutdruckSystolisch(1)
                .withBlutdruckDiastolisch(1)
                .withHerzauskultation(Herzauskultation.ANDERES, "Auffällig")
                .withHaut(Haut.ANDERES, "Auffällig")
                .withMundAndZaehne(List.of(MundAndZaehne.ANDERES), "Auffällig")
                .withSprachfaehigkeit(
                    List.of(Sprachfaehigkeit.ARTIKULATIONSFEHLER),
                    "Artikulationsfehler",
                    BigDecimal.valueOf(1))
                .withMotorik(Motorik.GUT)
                .withMotorikPunkte(BigDecimal.valueOf(10.0))
                .withKognition(Kognition.AUFFAELLIG, BigDecimal.valueOf(10.0))
                .withBewegungsapparat(List.of(Bewegungsapparat.ANDERES), "Auffällig")
                .withEinbeinstand(
                    Einbeinstand.ANDERES,
                    Einbeinstand.SICHER,
                    Einbeinstand.SICHER,
                    "Auffällig")
                .withEinbeinhuepfen(
                    Einbeinhuepfen.ANDERES,
                    Einbeinhuepfen.SICHER,
                    Einbeinhuepfen.SICHER,
                    "Auffällig")
                .withFingeropposition(List.of(Fingeropposition.ANDERES), "Auffällig")
                .withHampelmann(Hampelmann.MIND_FUENF_MAL)
                .withBallFangen(BallFangen.BALL_GEFANGEN)
                .withBallWerfen(BallWerfen.MIND_ZWEI_METER)
                .withNameSchreiben(NameSchreiben.GESCHAFFT)
                .withFigurNachmalen(FigurNachmalen.KORREKT)
                .withStifthaltung(Stifthaltung.OKAY)
                .withAussprache(Aussprache.OKAY)
                .withLesefit(Lesefit.PUNKTE_15_BIS_16, BigDecimal.valueOf(15.5))
                .withZahlenNachsprechen(ZahlenNachsprechen.RICHTIG, ZahlenNachsprechen.FALSCH)
                .withZeichnen(
                    Zeichnen.RICHTIG,
                    Zeichnen.RICHTIG,
                    Zeichnen.RICHTIG,
                    ZeichnenMensch.ERFUELLT,
                    BigDecimal.valueOf(10.0))
        );

        UntersuchungArztBeurteilungJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArztBeurteilung()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBeurteilungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData()
            .getEinverstaendniserklaerung()).containsExactly(EinverstaendniserklaerungSpec.SCHWEIGEPFLICHT_VORHANDEN);
        assertThat(result.getData().getVerhaltensfragebogen()).isEqualTo(VerhaltensfragebogenSpec.VORHANDEN);
        assertThat(result.getData().getVerhalten()).isEqualTo(VerhaltenSpec.AUFFAELLIG);
        assertThat(result.getData().getBlutdruck()).isEqualTo(BlutdruckSpec.NORMAL);
        assertThat(result.getData().getBlutdruckSystolisch()).isEqualTo(1);
        assertThat(result.getData().getBlutdruckDiastolisch()).isEqualTo(1);
        assertThat(result.getData().getHerzauskultation()).isEqualTo(HerzauskultationSpec.ANDERES);
        assertThat(result.getData().getHerzauskultationBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getHaut()).isEqualTo(HautSpec.ANDERES);
        assertThat(result.getData().getHautBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getMundAndZaehne()).containsExactly(MundAndZaehneSpec.ANDERES);
        assertThat(result.getData().getMundAndZaehneBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData()
            .getSprachfaehigkeitSingleValue()).isEqualTo(saveJsonSpec.getSprachfaehigkeitSingleValue());
        assertThat(result.getData()
            .getSprachfaehigkeitMultiValue()).containsExactly(SprachfaehigkeitSpec.ARTIKULATIONSFEHLER);
        assertThat(result.getData().getSprachfaehigkeitBemerkung()).isEqualTo("Artikulationsfehler");
        assertThat(result.getData().getSprachfaehigkeitPunkte()).isEqualTo(BigDecimal.valueOf(1));
        assertThat(result.getData().getMotorik()).isEqualTo(MotorikSpec.GUT);
        assertThat(result.getData().getMotorikPunkte()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(result.getData().getKognition()).isEqualTo(KognitionSpec.AUFFAELLIG);
        assertThat(result.getData().getKognitionPunkte()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(result.getData().getBewegungsapparat()).containsExactly(BewegungsapparatSpec.ANDERES);
        assertThat(result.getData().getBewegungsapparatBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getEinbeinstand()).isEqualTo(EinbeinstandSpec.ANDERES);
        assertThat(result.getData().getEinbeinstandLinks()).isEqualTo(EinbeinstandSpec.SICHER);
        assertThat(result.getData().getEinbeinstandRechts()).isEqualTo(EinbeinstandSpec.SICHER);
        assertThat(result.getData().getEinbeinstandBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getEinbeinhuepfen()).isEqualTo(EinbeinhuepfenSpec.ANDERES);
        assertThat(result.getData().getEinbeinhuepfenLinks()).isEqualTo(EinbeinhuepfenSpec.SICHER);
        assertThat(result.getData().getEinbeinhuepfenRechts()).isEqualTo(EinbeinhuepfenSpec.SICHER);
        assertThat(result.getData().getEinbeinhuepfenBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getFingeropposition()).containsExactly(FingeroppositionSpec.ANDERES);
        assertThat(result.getData().getFingeroppositionBemerkung()).isEqualTo("Auffällig");
        assertThat(result.getData().getHampelmann()).isEqualTo(HampelmannSpec.MIND_FUENF_MAL);
        assertThat(result.getData().getBallFangen()).isEqualTo(BallFangenSpec.BALL_GEFANGEN);
        assertThat(result.getData().getBallWerfen()).isEqualTo(BallWerfenSpec.MIND_ZWEI_METER);
        assertThat(result.getData().getNameSchreiben()).isEqualTo(NameSchreibenSpec.GESCHAFFT);
        assertThat(result.getData().getFigurNachmalen()).isEqualTo(FigurNachmalenSpec.KORREKT);
        assertThat(result.getData().getStifthaltung()).isEqualTo(StifthaltungSpec.OKAY);
        assertThat(result.getData().getAussprache()).isEqualTo(AusspracheSpec.OKAY);
        assertThat(result.getData().getLesefit()).isEqualTo(LesefitSpec.PUNKTE_15_BIS_16);
        assertThat(result.getData().getLesefitPunkte()).isEqualTo(BigDecimal.valueOf(15.5));
        assertThat(result.getData().getZahlenNachsprechen4()).isEqualTo(ZahlenNachsprechenSpec.RICHTIG);
        assertThat(result.getData().getZahlenNachsprechen5()).isEqualTo(ZahlenNachsprechenSpec.FALSCH);
        assertThat(result.getData().getZeichnenKreis()).isEqualTo(ZeichnenSpec.RICHTIG);
        assertThat(result.getData().getZeichnenViereck()).isEqualTo(ZeichnenSpec.RICHTIG);
        assertThat(result.getData().getZeichnenDreieck()).isEqualTo(ZeichnenSpec.RICHTIG);
        assertThat(result.getData().getZeichnenMensch()).isEqualTo(ZeichnenMenschSpec.ERFUELLT);
        assertThat(result.getData().getZeichnenMenschPunkte()).isEqualTo(BigDecimal.valueOf(10.0));
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungArztBemerkung_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);
        Benutzer benutzer = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(benutzer);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungArztBemerkung()).isNull();

        // ACT
        UntersuchungArztBemerkungSaveJsonSpec saveJsonSpec = UntersuchungArztBemerkungBuilder.createSaveJaxSpec(
            builder -> builder
                .withBemerkungen("Bemerkung")
        );

        UntersuchungArztBemerkungJsonSpec result = api
            .untersuchungArztResourceSaveUntersuchungArztBemerkung()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungArztBemerkungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isEqualTo("Bemerkung");
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
