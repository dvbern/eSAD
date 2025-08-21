package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewichtRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTabsSavedJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmenRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.NaechsteStation;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import java.io.File;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.EinladungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindIdListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlassenstufeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTabInfoListJsonSpec;
import io.quarkus.arc.ArcUndeclaredThrowableException;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class EinladungResourceITest extends AbstractITest {

    private final EinladungenApiSpec api = EinladungenApiSpec.einladungen(RequestSpecUtil.quarkusSpec());

    private final EinladungService einladungService;
    private final FixtureService fixtureService;
    private final UntersuchungRepo untersuchungRepo;
    private final UntersuchungKindDatenRepo untersuchungKindDatenRepo;
    private final UntersuchungDurchfuehrungRepo untersuchungDurchfuehrungRepo;
    private final UntersuchungImpfungenRepo untersuchungImpfungenRepo;
    private final UntersuchungTriageRepo untersuchungTriageRepo;
    private final UntersuchungHoerenAudiogrammRepo untersuchungHoerenAudiogrammRepo;
    private final UntersuchungHoerenOtoskopieRepo untersuchungHoerenOtoskopieRepo;
    private final UntersuchungSehenRepo untersuchungSehenRepo;
    private final UntersuchungImpfstatusKrankheitRepo untersuchungImpfstatusKrankheitRepo;
    private final UntersuchungImpfungInternRepo untersuchungImpfungInternRepo;
    private final NotizService notizService;
    private final UntersuchungMassnahmenRepo untersuchungMassnahmenRepo;
    private final UntersuchungGroesseGewichtRepo untersuchungGroesseGewichtRepo;
    private final UntersuchungArztRepo untersuchungArztRepo;
    private final AufgabeService aufgabeService;
    private final AnlassService anlassService;

    private final AppClock appClock;
    private final TL tl;

    private final ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findEinladungenByAnlass() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        final Anlass anotherAnlass = fixtureService.createAnlass();
        final Dossier dossier = fixtureService.createDossier();
        final Einladung einladung = einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier))
        );
        final Einladung anotherEinladung = einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anotherAnlass)
                .withDossier(dossier))
        );
        // ACT
        EinladungListJsonSpec result = api.einladungResourceFindEinladungenByAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungListJsonSpec.class);
        // ASSERT
        assertThat(result.getEinladungList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> einladung.getId().getId().equals(jsonSpec.getId()))
            .noneMatch(jsonSpec -> anotherEinladung.getId().getId().equals(jsonSpec.getId()));
    }

    @Test
    @TestAsAdminBS
    void findEinladungenByAnlassWithUntersuchungKindDaten() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithKindDaten();

        // ACT
        EinladungJsonSpec result = api.einladungResourceGetEinladungById()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getUntersuchungKindDaten()).isNotNull();
        assertThat(result.getUntersuchungKindDaten()
            .getKlassenstufe()).isEqualTo(KlassenstufeSpec.PRIMAR_KLASSE_4);
    }

    @Test
    @TestAsAdminBS
    void getEinladungById() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Dossier dossier = fixtureService.createDossier();
        Einladung einladung = EinladungBuilder.create(builder -> builder.withAnlass(anlass).withDossier(dossier));
        einladungService.persist(einladung);

        // ACT
        EinladungJsonSpec result = api
            .einladungResourceGetEinladungById()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat().statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(einladung.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateEinladung() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        assertThat(einladung.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        // ACT
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.NACHZUEGLER),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        EinladungJsonSpec result = api.einladungResourceUpdateEinladung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(jaxSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungJsonSpec.class);
        // ASSERT
        assertThat(result.getData().getEinladungStatus()).isEqualTo(EinladungStatusSpec.NACHZUEGLER);
    }

    @Test
    @TestAsAdminBS
    void updateEinladung_setAbmeldungsgrund() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        assertThat(einladung.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);

        // ACT
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withGrundAbmeldung(GrundAbmeldung.KONTROLLE_KINDER_AUGENARZT)
                .withArtAbmeldung(ArtAbmeldung.MAIL_LEHRPERSON)
                .withStatus(EinladungStatus.TEILNAHME_ABGESAGT),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getAbmeldungsNotiz()).isNotNull();
        assertThat(einladungChanged.getAbmeldungsNotiz().getBemerkungen())
            .contains("Grund: Kontrolle bei Kinderarzt/Kinderärztin oder Augenarzt/Augenärztin\n"
                + "Art der Abmeldung: Mail von Lehrperson");

    }

    @Test
    @TestAsAdminBS
    void updateEinladung_setStatus_Nachzuegler_creates_aufgabe() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        assertThat(einladung.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);

        // ACT
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.NACHZUEGLER),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.NACHZUEGLER);
        List<Aufgabe> aufgaben = aufgabeService.findAllForKind(einladungChanged.getDossier().getKind().getId());
        assertThat(aufgaben).hasSize(1);
        assertThat(aufgaben.getFirst().getTitel()).isEqualTo("Nachzügler Gerber Emma 2022/23 SU 4.PS Test getAnlass");
    }

    @Test
    @TestAsAdminBS
    void updateEinladung_setStatus_FromNachZuegler_closesAufgabe() {
        // ARRANGE Create Einladung and Update to Nachzuegler to create Aufgabe
        Einladung einladung = fixtureService.createEinladung();
        assertThat(einladung.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.NACHZUEGLER),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);
        // ASSERT That the Einladung Status was set to NACHZUEGLER and Aufgabe was created
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        List<Aufgabe> aufgaben = aufgabeService.findAufgabenForKindWithLabel(
            einladungChanged.getDossier().getKind(),
            Label.ID_LABEL_NACHZUEGLER);
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.NACHZUEGLER);
        assertThat(aufgaben).hasSize(1);

        // ACT Update the Einladung to TEILNAHME_BESTAETIGT so the Aufgabe gets closed
        jaxSpec.setEinladungStatus(EinladungStatusSpec.TEILNAHME_BESTAETIGT);
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT That the Einladung Status was set to TEILNAHME_BESTAETIGT and Aufgabe was closed
        einladungChanged = einladungService.getById(einladung.getId());
        aufgaben = aufgabeService.findAufgabenForKindWithLabel(
            einladungChanged.getDossier().getKind(), Label.ID_LABEL_NACHZUEGLER);
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        assertThat(aufgaben).hasSize(1);
        assertThat(aufgaben.getFirst().getAufgabeStatus()).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(aufgaben.getFirst().getTitel()).isEqualTo("Nachzügler Gerber Emma 2022/23 SU 4.PS Test getAnlass");
    }

    @Test
    @TestAsAdminBS
    void updateEinladung_setStatus_toNachzuegler_createsAufgabe_changeAgainClosesAufgabe_changeAgainToNachzuegler_reopensAufgabe() {
        // ARRANGE Create Einladung and Update to Nachzuegler to create Aufgabe
        Einladung einladung = fixtureService.createEinladung();
        assertThat(einladung.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.NACHZUEGLER),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);
        // ASSERT That the Einladung Status was set to NACHZUEGLER and Aufgabe was created
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.NACHZUEGLER);
        List<Aufgabe> aufgaben = aufgabeService.findAufgabenForKindWithLabel(
            einladungChanged.getDossier().getKind(),
            Label.ID_LABEL_NACHZUEGLER);
        assertThat(aufgaben).hasSize(1);
        // ARRANGE Update the Einladung to TEILNAHME_BESTAETIGT so the Aufgabe gets closed
        jaxSpec.setEinladungStatus(EinladungStatusSpec.TEILNAHME_BESTAETIGT);
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);
        // ASSERT That the Einladung Status was set to TEILNAHME_BESTAETIGT and Aufgabe is closed
        einladungChanged = einladungService.getById(einladung.getId());
        aufgaben = aufgabeService.findAufgabenForKindWithLabel(
            einladungChanged.getDossier().getKind(),
            Label.ID_LABEL_NACHZUEGLER);
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        assertThat(aufgaben).hasSize(1);
        assertThat(aufgaben.getFirst().getAufgabeStatus()).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);

        // ACT Set Status to NACHZUEGLER again so Aufgabe is reopened
        jaxSpec.setEinladungStatus(EinladungStatusSpec.NACHZUEGLER);
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT That the Einladung Status was set to NACHZUEGLER and Aufgabe is reopened
        einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getEinladungStatus()).isEqualTo(EinladungStatus.NACHZUEGLER);
        aufgaben = aufgabeService.findAufgabenForKindWithLabel(
            einladungChanged.getDossier().getKind(), Label.ID_LABEL_NACHZUEGLER);
        assertThat(aufgaben).hasSize(1);
        assertThat(aufgaben.getFirst().getAufgabeStatus()).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(aufgaben.getFirst().getTitel()).isEqualTo("Nachzügler Gerber Emma 2022/23 SU 4.PS Test getAnlass");
    }

    @Test
    @TestAsAdminBS
    void updateEinladung_deleteAbmeldungsnotiz_teilnahmestatus_is_not_abgemeldet() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladungWithStatusAbgesagt();
        assertThat(einladung.getAbmeldungsNotiz()).isNotNull();

        // ACT
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.TEILNAHME_BESTAETIGT),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getAbmeldungsNotiz()).isNull();
    }

    @Test
    @TestAsAdminBS
    void updateEinladung_updateAbmeldungsNotiz() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladungWithStatusAbgesagt();
        assertThat(einladung.getAbmeldungsNotiz()).isNotNull();

        // ACT
        EinladungSaveJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung)
                .withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
                .withGrundAbmeldung(GrundAbmeldung.KEINE_ABMELDUNG),
            appClock.offsetDateTime().now().toLocalDate()).getData();
        updateEinladung(einladung.getId(), einladung.getAnlass().getId(), jaxSpec);

        // ASSERT
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getAbmeldungsNotiz()).isNotNull();
        assertThat(einladungChanged.getAbmeldungsNotiz()
            .getBemerkungen()).contains(tl.translate("GrundAbmeldung." + GrundAbmeldung.KEINE_ABMELDUNG));
    }

    @Test
    @TestAsAdminBS
    void createImpfausweisNotiz() throws JsonProcessingException {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        // ACT
        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Impfausweis")
            .withVerantwortlicher(fixtureService.createBenutzer())
            .withDossier(einladung.getDossier())).getData();
        String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);
        NotizJsonSpec result = api.einladungResourceCreateImpfausweisNotiz()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
            })
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(NotizJsonSpec.class);
        // ASSERT
        assertThat(result.getData().getTitel()).isEqualTo("Impfausweis");
        assertThat(result.getData().getLabels())
            .extracting(LabelJsonSpec::getId)
            .contains(Label.ID_LABEL_IMPFAUSWEIS.getId());
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getImpfausweis()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void createImpfausweisNotizWithFiles() throws IOException {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        File testFile = TestUtil.loadTestImage();
        byte[] testFileBytes = Files.readAllBytes(TestUtil.loadTestPDF().toPath());

        // ACT
        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Impfausweis with File")
            .withVerantwortlicher(fixtureService.createBenutzer())
            .withDossier(einladung.getDossier())).getData();

        String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);

        NotizJsonSpec result = api.einladungResourceCreateImpfausweisNotiz()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
            })
            ._fileMultiPart(testFile)
            // using the generated file2 method does not work, it seems to be using the 'file' name as form name
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file2", "terstfile2name.pdf" ,testFileBytes);
            })
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file3", "terstfile3name.pdf" ,testFileBytes);
            })
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file4", "terstfile4name.pdf" ,testFileBytes);
            })
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        // ASSERT
        assertThat(result.getData().getTitel()).isEqualTo("Impfausweis with File");
        assertThat(result.getData().getLabels())
            .extracting(LabelJsonSpec::getId)
            .contains(Label.ID_LABEL_IMPFAUSWEIS.getId());
        assertThat(result.getData().getFiles())
            .isNotEmpty()
            .hasSize(4);

        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getImpfausweis()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void createFragebogenNotiz() throws JsonProcessingException {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        // ACT
        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Fragebogen")
            .withVerantwortlicher(fixtureService.createBenutzer())
            .withDossier(einladung.getDossier())).getData();
        String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);
        NotizJsonSpec result = api.einladungResourceCreateFragebogenNotiz()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
            })
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(NotizJsonSpec.class);
        // ASSERT
        assertThat(result.getData().getTitel()).isEqualTo("Fragebogen");
        assertThat(result.getData().getLabels())
            .extracting(LabelJsonSpec::getId)
            .contains(Label.ID_LABEL_FRAGEBOGEN.getId());
        Einladung einladungChanged = einladungService.getById(einladung.getId());
        assertThat(einladungChanged.getFragebogen()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void createFragebogenNotizAllAtOnce() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        File testFile = TestUtil.loadTestImage();

        // ACT
        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Fragebogen with File")
            .withVerantwortlicher(fixtureService.createBenutzer())
            .withDossier(einladung.getDossier())).getData();

        try {
            String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);

            NotizJsonSpec result = api.einladungResourceCreateFragebogenNotiz()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .reqSpec(spec -> {
                    spec.setContentType("multipart/form-data");
                    spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
                })
                ._fileMultiPart(testFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().as(NotizJsonSpec.class);

            // ASSERT
            assertThat(result.getData().getTitel()).isEqualTo("Fragebogen with File");
            assertThat(result.getData().getLabels())
                .extracting(LabelJsonSpec::getId)
                .contains(Label.ID_LABEL_FRAGEBOGEN.getId());
            assertThat(result.getData().getFiles())
                .isNotEmpty()
                .hasSize(1);

            Einladung einladungChanged = einladungService.getById(einladung.getId());
            assertThat(einladungChanged.getFragebogen()).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException("Error converting NotizSaveJsonSpec to JSON", e);
        }
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungTabInfos() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        Einladung einladung1 = fixtureService.createEinladung(anlass);
        Einladung einladung2 = fixtureService.createEinladung(anlass);
        Einladung einladung3 = fixtureService.createEinladung(anlass);
        // ACT
        UntersuchungTabInfoListJsonSpec result = api.einladungResourceGetUntersuchungTabInfos()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdsQuery(List.of(einladung1.getId().getId(), einladung3.getId().getId()).toArray())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTabInfoListJsonSpec.class);
        // ASSERT
        assertThat(result.getTabInfoList())
            .isNotEmpty()
            .hasSize(2)
            .anyMatch(jsonSpec -> einladung1.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung1.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()))
            .noneMatch(jsonSpec -> einladung2.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung2.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()))
            .anyMatch(jsonSpec -> einladung3.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung3.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()));
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungTabsSavedJson_fromUntersuchung() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehen();
        // ACT
        UntersuchungTabsSavedJson untersuchungTabsSavedJson =
            EinladungMapper.toUntersuchungTabsSavedJson(untersuchung, List.of(), List.of());

        // ASSERT
        assertThat(untersuchungTabsSavedJson).isNotNull();
        assertThat(untersuchungTabsSavedJson.getUntersuchungID()).isEqualTo(untersuchung.getId());
        assertThat(untersuchungTabsSavedJson.isDatenTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isImpfungenTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isSehenTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isHoerenAudiogrammTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isHoerenOtoskopieTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isGroesseGewichtTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isTriageTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isAerzlicheUnteruchungTabSaved()).isFalse();
        assertThat(untersuchungTabsSavedJson.isMassnahmenTabSaved()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungTabsSavedJson_fromFullUntersuchung() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithEveryTab();
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        List<UntersuchungImpfstatusKrankheit> impfStatus =
            List.of(fixtureService.createUntersuchungImpfstatusKrankheit(untersuchung.getUntersuchungImpfungen()));

        // ACT
        UntersuchungTabsSavedJson untersuchungTabsSavedJson =
            EinladungMapper.toUntersuchungTabsSavedJson(untersuchung, impfStatus, List.of());

        // ASSERT
        assertThat(untersuchungTabsSavedJson).isNotNull();
        assertThat(untersuchungTabsSavedJson.getUntersuchungID()).isEqualTo(untersuchung.getId());
        assertThat(untersuchungTabsSavedJson.isDatenTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isImpfungenTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isSehenTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isHoerenAudiogrammTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isHoerenOtoskopieTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isGroesseGewichtTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isTriageTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isAerzlicheUnteruchungTabSaved()).isTrue();
        assertThat(untersuchungTabsSavedJson.isMassnahmenTabSaved()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getHiddenUntersuchungTabInfos() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        Einladung einladung1 = fixtureService.createEinladung(anlass);
        Einladung einladung2 = fixtureService.createEinladung(anlass);
        Einladung einladung3 = fixtureService.createEinladung(anlass);
        Einladung einladung4 = fixtureService.createEinladung(anlass);
        // ACT
        UntersuchungTabInfoListJsonSpec result = api.einladungResourceGetHiddenUntersuchungTabInfos()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdsQuery(List.of(einladung1.getId().getId(), einladung3.getId().getId()).toArray())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTabInfoListJsonSpec.class);
        // ASSERT
        assertThat(result.getTabInfoList())
            .isNotEmpty()
            .hasSize(2)
            .noneMatch(jsonSpec -> einladung1.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung1.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()))
            .anyMatch(jsonSpec -> einladung2.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung2.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()))
            .noneMatch(jsonSpec -> einladung3.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung3.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()))
            .anyMatch(jsonSpec -> einladung4.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung4.getDossier().getKind().getId().getId().equals(jsonSpec.getKindID()));
    }

    @Test
    @TestAsAdminBS
    void getTabInfosAndHiddenTabInfosNoDuplicates() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        Einladung einladung1 = fixtureService.createEinladung(anlass);
        Einladung einladung2 = fixtureService.createEinladung(anlass);
        Einladung einladung3 = fixtureService.createEinladung(anlass);
        Einladung einladung4 = fixtureService.createEinladung(anlass);
        Einladung einladung5 = fixtureService.createEinladung(anlass);
        Object[] params = List.of(einladung1.getId().getId(), einladung3.getId().getId()).toArray();

        // ACT
        UntersuchungTabInfoListJsonSpec resultTabs = api.einladungResourceGetUntersuchungTabInfos()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdsQuery(params)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTabInfoListJsonSpec.class);
        UntersuchungTabInfoListJsonSpec resultHiddenTabs = api.einladungResourceGetHiddenUntersuchungTabInfos()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdsQuery(params)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTabInfoListJsonSpec.class);
        // ASSERT
        assertThat(resultTabs.getTabInfoList())
            .doesNotContainAnyElementsOf(resultHiddenTabs.getTabInfoList());
        assertThat(resultTabs.getTabInfoList())
            .isNotEmpty()
            .hasSize(2)
            .anyMatch(jsonSpec -> einladung1.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung3.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung2.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung4.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung5.getId().getId().equals(jsonSpec.getEinladungID()));
        assertThat(resultHiddenTabs.getTabInfoList())
            .isNotEmpty()
            .hasSize(3)
            .anyMatch(jsonSpec -> einladung2.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung4.getId().getId().equals(jsonSpec.getEinladungID()))
            .anyMatch(jsonSpec -> einladung5.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung1.getId().getId().equals(jsonSpec.getEinladungID()))
            .noneMatch(jsonSpec -> einladung3.getId().getId().equals(jsonSpec.getEinladungID()));

    }

    @Test
    @TestAsAdminBS
    void createEinladungenForAnlass() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        final Kind kind = fixtureService.createKind();
        final Kind kind2 = fixtureService.createKind();
        KindIdListJsonSpec kindIdListJsonSpec = new KindIdListJsonSpec(
            List.of(
                kind.getId().getId(), kind2.getId().getId()
            )
        );
        // ACT
        EinladungListJsonSpec result = api.einladungResourceCreateEinladungenForAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(kindIdListJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungListJsonSpec.class);
        // ASSERT
        assertThat(result.getEinladungList())
            .isNotEmpty()
            .hasSize(2)
            .anyMatch(jsonSpec -> kind.getId().getId().equals(jsonSpec.getKind().getId()))
            .anyMatch(jsonSpec -> kind2.getId().getId().equals(jsonSpec.getKind().getId()));
    }

    @Test
    @TestAsAdminBS
    void createEinladungNachzuegler_should_move_impfausweis_to_new_einladung() {
        // ARRANGE
        String titleAnlassNichtTeilgenommen = "Mein Anlass nicht Teilgenommen";
        AnlassConfig anlassConfig = fixtureService.createAnlassConfigAllOptional(AnlassTyp.KLASSE_4);
        final Anlass anlassNichtTeilgenommen = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle(titleAnlassNichtTeilgenommen)
                .withAnlassConfig(anlassConfig)
                .withDatumSetSchuljahr(anlassConfig.getGueltigkeitsZeitraum().getDatumVon().plusDays(1))
                .withAnlassTyp(AnlassTyp.KLASSE_4))
        );
        Dossier dossier = fixtureService.createDossier();
        Einladung einladungNichtTeilgenommen = fixtureService.createEinladung(anlassNichtTeilgenommen, dossier, EinladungStatus.NACHZUEGLER);
        fixtureService.createImpfausweisNotiz(einladungNichtTeilgenommen.getId());
        fixtureService.createFragebogenNotiz(einladungNichtTeilgenommen.getId());
        einladungNichtTeilgenommen = einladungService.getById(einladungNichtTeilgenommen.getId());
        assertThat(einladungNichtTeilgenommen.getImpfausweis()).isNotNull();
        assertThat(einladungNichtTeilgenommen.getFragebogen()).isNotNull();
        ID<Notiz> notizImpfausweisID = einladungNichtTeilgenommen.getImpfausweis().getId();
        ID<Notiz> notizFragebogenID = einladungNichtTeilgenommen.getFragebogen().getId();

        // ACT
        String titleAnlassAlsNachzuegler = "Mein Anlass Nachzügler";
        final Anlass anlassAlsNachzuegler = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle(titleAnlassAlsNachzuegler)
                .withAnlassConfig(anlassConfig)
                .withDatumSetSchuljahr(anlassConfig.getGueltigkeitsZeitraum().getDatumVon().plusDays(5))
                .withAnlassTyp(AnlassTyp.KLASSE_4))
        );
        KindIdListJsonSpec kindIdListJsonSpec = new KindIdListJsonSpec(
            List.of(dossier.getKind().getId().getId())
        );
        EinladungListJsonSpec result = api.einladungResourceCreateEinladungenForAnlass()
            .anlassIdPath(anlassAlsNachzuegler.getId().getId())
            .body(kindIdListJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungListJsonSpec.class);

        // ASSERT
        assertThat(result.getEinladungList())
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(jsonSpec -> dossier.getKind().getId().getId().equals(jsonSpec.getKind().getId()));
        // Reload Data
        Einladung einladungNichtTeilgenommenReloaded = einladungService.getById(einladungNichtTeilgenommen.getId());
        ID<Einladung> einladungAlsNachzueglerID = ID.of(UUID.fromString(result.getEinladungList().getFirst().getId()), Einladung.class);
        Einladung einladungAlsNachzuegler = einladungService.getById(einladungAlsNachzueglerID);
        assertThat(einladungNichtTeilgenommenReloaded.getEinladungStatus()).isEqualTo(EinladungStatus.NACHZUEGLER);
        assertThat(einladungAlsNachzuegler.getEinladungStatus()).isEqualTo(EinladungStatus.TEILNAHME_BESTAETIGT);
        // Die Notizen wurden von der alten Einladung auf die neue Einladung verschoben
        assertThat(einladungNichtTeilgenommenReloaded.getImpfausweis()).isNull();
        assertThat(einladungNichtTeilgenommenReloaded.getFragebogen()).isNull();
        assertThat(einladungAlsNachzuegler.getImpfausweis()).isNotNull();
        assertThat(einladungAlsNachzuegler.getFragebogen()).isNotNull();

        assertThat(einladungAlsNachzuegler.getImpfausweis().getDossier()).isEqualTo(einladungNichtTeilgenommen.getImpfausweis().getDossier());
        assertThat(einladungAlsNachzuegler.getFragebogen().getDossier()).isEqualTo(einladungNichtTeilgenommen.getFragebogen().getDossier());
        assertThat(einladungAlsNachzuegler.getImpfausweis().getStatus()).isEqualTo(einladungNichtTeilgenommen.getImpfausweis().getStatus());
        assertThat(einladungAlsNachzuegler.getFragebogen().getStatus()).isEqualTo(einladungNichtTeilgenommen.getFragebogen().getStatus());
        // Im Titel steht der Name des neuen Anlasses
        assertThat(einladungAlsNachzuegler.getImpfausweis().getTitel()).contains(titleAnlassAlsNachzuegler);
        assertThat(einladungAlsNachzuegler.getFragebogen().getTitel()).contains(titleAnlassAlsNachzuegler);
        // In der Bemerkung steht der Titel des Anlasses, der nicht besucht wurde
        assertThat(einladungAlsNachzuegler.getImpfausweis().getBemerkungen()).contains(titleAnlassNichtTeilgenommen);
        assertThat(einladungAlsNachzuegler.getFragebogen().getBemerkungen()).contains(titleAnlassNichtTeilgenommen);
    }

    @Test
    @TestAsAdminBS
    void removeEinladungenFromAnlass() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        final Dossier dossier = fixtureService.createDossier();
        Einladung einladung = einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier))
        );
        EinladungJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung),
            appClock.offsetDateTime().now().toLocalDate());
        EinladungListJsonSpec kindListJsonSpec = new EinladungListJsonSpec(List.of(jaxSpec));
        // ACT
        api.einladungResourceRemoveEinladungenForAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(kindListJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
        // ASSERT
        List<Einladung> einladungList = einladungService.findByAnlass(anlass.getId());
        assertThat(einladungList)
            .isEmpty();
    }

    @Test
    @TestAsAdminBS
    void removeDossierFromAnlass_withUntersuchung_should_delete_everything() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        fixtureService.createUntersuchungImpfungIntern(untersuchung.getUntersuchungImpfungen());
        fixtureService.createUntersuchungImpfstatusKrankheit(untersuchung.getUntersuchungImpfungen());
        fixtureService.createImpfausweisNotiz(untersuchung.getEinladung().getId());
        fixtureService.createFragebogenNotiz(untersuchung.getEinladung().getId());
        fixtureService.createAbmeldungsgrundNotiz(untersuchung.getEinladung().getId());

        untersuchung = untersuchungRepo.getById(untersuchung.getId());

        assertThat(untersuchung.getUntersuchungKindDaten()).isNotNull();
        assertThat(untersuchung.getUntersuchungDurchfuehrung()).isNotNull();
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();
        assertThat(untersuchungImpfstatusKrankheitRepo.findByUntersuchung(untersuchung.getId())).isNotEmpty();
        assertThat(untersuchungImpfungInternRepo.findByUntersuchung(untersuchung.getId())).isNotEmpty();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungHoerenAudiogramm()).isNotNull();
        assertThat(untersuchung.getUntersuchungHoerenOtoskopie()).isNotNull();
        assertThat(untersuchung.getUntersuchungSehen()).isNotNull();
        assertThat(untersuchung.getUntersuchungTriage()).isNotNull();
        assertThat(untersuchung.getUntersuchungGroesseGewicht()).isNotNull();
        assertThat(untersuchung.getUntersuchungArzt()).isNotNull();
        assertThat(untersuchung.getEinladung().getImpfausweis()).isNotNull();
        ID<Notiz> notizImpfausweisId = untersuchung.getEinladung().getImpfausweis().getId();
        assertThat(notizService.getById(notizImpfausweisId)).isNotNull();
        assertThat(untersuchung.getEinladung().getFragebogen()).isNotNull();
        ID<Notiz> notizFragebogenId = untersuchung.getEinladung().getFragebogen().getId();
        assertThat(notizService.getById(notizFragebogenId)).isNotNull();
        assertThat(untersuchung.getEinladung().getAbmeldungsNotiz()).isNotNull();
        ID<Notiz> notizAbmeldungsgrundId = untersuchung.getEinladung().getAbmeldungsNotiz().getId();
        assertThat(notizService.getById(notizAbmeldungsgrundId)).isNotNull();
        // ACT
        List<ID<Einladung>> einladungIdList = List.of(untersuchung.getEinladung().getId());
        einladungService.removeEinladungen(einladungIdList);
        // ASSERT
        assertThat(untersuchungKindDatenRepo.findById(untersuchung.getUntersuchungKindDaten().getId())).isEmpty();
        assertThat(untersuchungDurchfuehrungRepo.findById(untersuchung.getUntersuchungDurchfuehrung()
            .getId())).isEmpty();
        assertThat(untersuchungImpfungenRepo.findById(untersuchung.getUntersuchungImpfungen().getId())).isEmpty();
        assertThat(untersuchungImpfstatusKrankheitRepo.findByUntersuchung(untersuchung.getId())).isEmpty();
        assertThat(untersuchungImpfungInternRepo.findByUntersuchung(untersuchung.getId())).isEmpty();
        assertThat(untersuchungHoerenAudiogrammRepo.findById(untersuchung.getUntersuchungHoerenAudiogramm()
            .getId())).isEmpty();
        assertThat(untersuchungHoerenOtoskopieRepo.findById(untersuchung.getUntersuchungHoerenOtoskopie()
            .getId())).isEmpty();
        assertThat(untersuchungSehenRepo.findById(untersuchung.getUntersuchungSehen().getId())).isEmpty();
        assertThat(untersuchungTriageRepo.findById(untersuchung.getUntersuchungTriage().getId())).isEmpty();
        assertThat(untersuchungGroesseGewichtRepo.findById(untersuchung.getUntersuchungGroesseGewicht()
            .getId())).isEmpty();
        assertThat(untersuchungRepo.findById(untersuchung.getId())).isEmpty();
        assertThat(notizService.findById(notizImpfausweisId)).isEmpty();
        assertThat(notizService.findById(notizFragebogenId)).isEmpty();
        assertThat(notizService.findById(notizAbmeldungsgrundId)).isEmpty();
        assertThat(untersuchungMassnahmenRepo.findById(untersuchung.getUntersuchungMassnahmen().getId())).isEmpty();
        assertThat(untersuchungArztRepo.findById(untersuchung.getUntersuchungArzt().getId())).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void updateDossiersManyChanges() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        final Dossier dossier1 = fixtureService.createDossier();
        einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier1)));
        final Dossier dossier2 = fixtureService.createDossier();
        einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier2)));
        final Dossier dossier3 = fixtureService.createDossier();
        // Vorher sind die Dossiers 1 und 2 dem Anlass zugeordnet, Dossier 3 nicht
        List<Einladung> einladungenVorher = einladungService.findByAnlass(anlass.getId());
        assertThat(einladungenVorher)
            .extracting(Einladung::getDossier)
            .hasSize(2)
            .containsExactlyInAnyOrder(dossier1, dossier2)
            .doesNotContain(dossier3);
        // ACT
        // Dossier 3 wird hinzugefügt
        KindIdListJsonSpec kindIdListJsonSpec = new KindIdListJsonSpec(List.of(dossier3.getKind().getId().getId()));
        api.einladungResourceCreateEinladungenForAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(kindIdListJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungListJsonSpec.class);
        // Dossier 2 entfernen
        Einladung einladung2 =
            einladungenVorher.stream().filter(einl -> einl.getDossier().equals(dossier2)).findFirst().orElseThrow();
        EinladungJsonSpec jaxSpec = EinladungBuilder.createJaxSpec(
            builder -> builder
                .forEinladung(einladung2),
            appClock.offsetDateTime().now().toLocalDate());
        EinladungListJsonSpec einladungListToRemove = new EinladungListJsonSpec(List.of(jaxSpec));
        // ACT
        api.einladungResourceRemoveEinladungenForAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(einladungListToRemove)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        List<Einladung> einladungenNachher = einladungService.findByAnlass(anlass.getId());
        assertThat(einladungenNachher)
            .extracting(Einladung::getDossier)
            .hasSize(2)
            .containsExactlyInAnyOrder(dossier1, dossier3)
            .doesNotContain(dossier2);
    }

    @Test
    @TestAsAdminBS
    void duplicateDossier() {
        // ARRANGE
        final Anlass anlass = fixtureService.createAnlass();
        final Dossier dossier = fixtureService.createDossier();
        einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier)));
        // ACT / ASSERT
        Einladung einladungForSameDossierAndAnlass = EinladungBuilder.create(builder -> builder
            .withAnlass(anlass)
            .withDossier(dossier));
        ArcUndeclaredThrowableException ex = assertThrows(
            ArcUndeclaredThrowableException.class,
            () -> einladungService.persist(einladungForSameDossierAndAnlass)
        );
        assertThat(ExceptionUtils.getRootCause(ex))
            .hasMessageContaining("duplicate key value violates unique constraint \"uc_einladung_anlass_dossier\"");
    }

    @Test
    @TestAsAdminBS
    void untersuchungAbschliessen_empty_untersuchung() {
        // ARRANGE
        Benutzer mpa = fixtureService.createBenutzer();
        Anlass anlass = fixtureService.createAnlass(mpa);
        final Dossier dossier = fixtureService.createDossier();
        Einladung einladung = einladungService.persist(
            EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(dossier))
        );
        // ACT
        api.einladungResourceUntersuchungAbschliessen()
            .anlassIdPath(anlass.getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
        // ASSERT
        Untersuchung untersuchungAbgeschlossen = fixtureService.getUntersuchungWithLoadedData(einladung.getId());
        Einladung einladungAbgeschlossen = untersuchungAbgeschlossen.getEinladung();
        assertThat(einladungAbgeschlossen.getNaechsteStation()).isEqualTo(NaechsteStation.FERTIG);
        assertThat(einladungAbgeschlossen.getNaechsteStationSeit()).isNotNull();
        // Durchfuehrung
        UntersuchungDurchfuehrung durchfuehrung = untersuchungAbgeschlossen.getUntersuchungDurchfuehrung();
        assertThat(durchfuehrung).isNotNull();
        assertThat(durchfuehrung.getArzt()).isNull();
        assertThat(durchfuehrung.getMpa()).isNotNull().isEqualTo(einladung.getAnlass().getMpa());
        // Impfen
        UntersuchungImpfungen impfungen = untersuchungAbgeschlossen.getUntersuchungImpfungen();
        assertThat(impfungen).isNotNull();
        // Hoeren
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchungAbgeschlossen.getUntersuchungHoerenAudiogramm();
        assertThat(hoerenAudiogramm).isNotNull();
        assertThat(hoerenAudiogramm.getAudiogrammBeurteilung()).containsExactly(AudiogrammBeurteilung.NICHT_GEPRUEFT);
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchungAbgeschlossen.getUntersuchungHoerenOtoskopie();
        assertThat(hoerenOtoskopie).isNotNull();
        assertThat(hoerenOtoskopie.getOtoskopieLinks()).containsExactly(OtoskopieResultat.NICHT_GEPRUEFT);
        // Sehen
        UntersuchungSehen sehen = untersuchungAbgeschlossen.getUntersuchungSehen();
        assertThat(sehen).isNotNull();
        assertThat(sehen.getBrilleLinsen()).isEqualTo(BrilleLinsenResultat.NICHT_GEPRUEFT);
        // GroesseGewicht
        UntersuchungGroesseGewicht groesseGewicht = untersuchungAbgeschlossen.getUntersuchungGroesseGewicht();
        assertThat(groesseGewicht).isNotNull();
        assertThat(groesseGewicht.getGroessePerzentile()).isEqualTo(PerzentileGroesse.NICHT_GEPRUEFT);
        // Triage
        UntersuchungTriage triage = untersuchungAbgeschlossen.getUntersuchungTriage();
        assertThat(triage).isNotNull();
        // Arzt
        UntersuchungArzt arzt = untersuchungAbgeschlossen.getUntersuchungArzt();
        assertThat(arzt).isNotNull();
        assertThat(arzt.getBewegungsapparat()).containsExactly(Bewegungsapparat.NICHT_GEPRUEFT);
        // Massnahmen
        UntersuchungMassnahmen massnahmen = untersuchungAbgeschlossen.getUntersuchungMassnahmen();
        assertThat(massnahmen).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void untersuchungAbschliessen_filled_untersuchung() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithCompleteData();
        // ACT
        api.einladungResourceUntersuchungAbschliessen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
        // ASSERT
        Untersuchung untersuchungAbgeschlossen =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        Einladung einladungAbgeschlossen = untersuchungAbgeschlossen.getEinladung();
        assertThat(einladungAbgeschlossen.getNaechsteStation()).isEqualTo(NaechsteStation.FERTIG);
        assertThat(einladungAbgeschlossen.getNaechsteStationSeit()).isNotNull();
        // Durchfuehrung
        UntersuchungDurchfuehrung durchfuehrung = untersuchungAbgeschlossen.getUntersuchungDurchfuehrung();
        assertThat(durchfuehrung).isNotNull();
        assertThat(durchfuehrung.getArzt()).isNotNull();
        assertThat(durchfuehrung.getMpa()).isNotNull();
        // Impfen
        UntersuchungImpfungen impfungen = untersuchungAbgeschlossen.getUntersuchungImpfungen();
        assertThat(impfungen).isNotNull();
        // Hoeren
        UntersuchungHoerenAudiogramm hoeren = untersuchungAbgeschlossen.getUntersuchungHoerenAudiogramm();
        assertThat(hoeren).isNotNull();
        assertThat(hoeren.getAudiogrammBeurteilung()).containsExactly(AudiogrammBeurteilung.NORMAL);
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchungAbgeschlossen.getUntersuchungHoerenOtoskopie();
        assertThat(hoerenOtoskopie).isNotNull();
        assertThat(hoerenOtoskopie.getOtoskopieLinks()).containsExactly(OtoskopieResultat.NICHT_EINSEHBAR);
        assertThat(hoerenOtoskopie.getOtoskopieRechts()).containsExactly(OtoskopieResultat.NORMAL);
        // Sehen
        UntersuchungSehen sehen = untersuchungAbgeschlossen.getUntersuchungSehen();
        assertThat(sehen).isNotNull();
        assertThat(sehen.getBrilleLinsen()).isEqualTo(BrilleLinsenResultat.OHNE_KEINE);
        // GroesseGewicht
        UntersuchungGroesseGewicht groesseGewicht = untersuchungAbgeschlossen.getUntersuchungGroesseGewicht();
        assertThat(groesseGewicht).isNotNull();
        assertThat(groesseGewicht.getGroessePerzentile()).isEqualTo(PerzentileGroesse.NORMAL);
        // Triage
        UntersuchungTriage triage = untersuchungAbgeschlossen.getUntersuchungTriage();
        assertThat(triage).isNotNull();
        // Arzt
        UntersuchungArzt arzt = untersuchungAbgeschlossen.getUntersuchungArzt();
        assertThat(arzt).isNotNull();
        assertThat(arzt.getBewegungsapparat()).containsExactlyInAnyOrder(
            Bewegungsapparat.SKOLIOSE,
            Bewegungsapparat.FUSSFEHLSTELLUNG);
        // Massnahmen
        UntersuchungMassnahmen massnahmen = untersuchungAbgeschlossen.getUntersuchungMassnahmen();
        assertThat(massnahmen).isNotNull();
    }

    void updateEinladung(
        @NonNull ID<Einladung> einladungId,
        @NonNull ID<Anlass> anlassId,
        @NonNull EinladungSaveJsonSpec jaxSpec
    ) {
        api.einladungResourceUpdateEinladung()
            .anlassIdPath(anlassId.getId())
            .einladungIdPath(einladungId.getId())
            .body(jaxSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(EinladungJsonSpec.class);
    }
}
