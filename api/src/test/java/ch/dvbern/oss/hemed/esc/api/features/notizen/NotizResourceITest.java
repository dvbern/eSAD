/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.notizen;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.FailureType;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelMapper;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.LabelJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@QuarkusTest
@RequiredArgsConstructor
class NotizResourceITest extends AbstractITest {

    private final NotizenApiSpec api = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    private final NotizService notizService;
    private final KindService kindService;
    private final DossierService dossierService;
    private final BenutzerService benutzerService;
    private final FixtureService fixtureService;
    private final FileStoreService fileStoreService;
    private final LabelService labelService;
    private final ObjectMapper objectMapper;

    private Benutzer verantwortlicher;
    private ID<Dossier> dossierId;
    private ID<Kind> kindId;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // Create a test dossier for all tests
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier dossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        dossierId = dossier.getId();
        kindId = dossier.getKind().getId();

        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("viktor.verantworltich@example.com")
                .withRandomVorname("Viktor")
                .withRandomNachname("Verantwortlich")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        var benId = benutzerService.createBenutzer(createDto);
        verantwortlicher = benutzerService.getById(benId);
    }

    @Test
    @TestAsAdminBS
    void createNotiz() throws IOException {
        // ARRANGE
        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Neue Notiz")
            .withBemerkungen("Neue Bemerkungen")
            .withVerantwortlicher(verantwortlicher)
            .withDossier(dossierService.getById(dossierId))).getData();
        File testFile = TestUtil.loadTestImage();
        byte[] testFileBytes = Files.readAllBytes(TestUtil.loadTestPDF().toPath());
        String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);

        // ACT
        NotizJsonSpec result = api
            .kinderNotizResourceCreate()
            .kindIdPath(kindId.getId())
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
            })
            ._fileMultiPart(testFile)
            // using the generated file2 method does not work, it seems to be using the 'file' name as form name
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file2", "terstfile2name.pdf", testFileBytes);
            })
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file3", "terstfile3name.pdf", testFileBytes);
            })
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("file4", "terstfile4name.pdf", testFileBytes);
            })
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getTitel()).isEqualTo("Neue Notiz");
        assertThat(result.getData().getBemerkungen()).isEqualTo("Neue Bemerkungen");
        assertThat(result.getData().getVerantwortlicher().getId()).isEqualTo(verantwortlicher.getId().getId());
        assertThat(result.getData().getFiles()).isNotEmpty().hasSize(4);
    }

    @Test
    @TestAsAdminBS
    void createNotiz_forInactiveKind_shouldThrow() throws IOException {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();
        Dossier dossier = fixtureService.createDossier(kind);

        NotizSaveJsonSpec saveNotizJson = NotizBuilder.createJaxSpec(builder -> builder
            .withTitel("Neue Notiz")
            .withBemerkungen("Neue Bemerkungen")
            .withVerantwortlicher(verantwortlicher)
            .withDossier(dossier)).getData();
        File testFile = TestUtil.loadTestImage();
        byte[] testFileBytes = Files.readAllBytes(TestUtil.loadTestPDF().toPath());
        String saveNotizJsonString = objectMapper.writeValueAsString(saveNotizJson);
        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es kann keine Notiz für ein inaktives Kind erstellt werden.",
            kind.getNameVorname(),
            kind.getId());

        // ACT & ASSERT
        AppValidationErrorJsonSpec validationError =  api
                .kinderNotizResourceCreate()
                .kindIdPath(kind.getId().getId())
                .reqSpec(spec -> {
                    spec.setContentType("multipart/form-data");
                    spec.addMultiPart("notizCreateJson", saveNotizJsonString, "application/json");
                })
                ._fileMultiPart(testFile)
                // using the generated file2 method does not work, it seems to be using the 'file' name as form name
                .reqSpec(spec -> {
                    spec.setContentType("multipart/form-data");
                    spec.addMultiPart("file2", "terstfile2name.pdf", testFileBytes);
                })
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotCeateEntityForInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void findAllNotizen() {
        // ARRANGE
        Notiz notizKind1 = notizService.persist(NotizBuilder.create(
            builder -> builder
                .withTitel("Test Notiz 1")
                .withDossier(fixtureService.createDossier())
                .withVerantwortlicher(verantwortlicher)));
        Notiz notizKind2 = notizService.persist(NotizBuilder.create(
            builder -> builder
                .withTitel("Test Notiz 2")
                .withDossier(fixtureService.createDossier())
                .withVerantwortlicher(verantwortlicher)));

        // ACT
        NotizListJsonSpec result = api
            .notizResourceFindAll()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizListJsonSpec.class);

        // ASSERT
        assertThat(result.getNotizen())
            .isNotEmpty()
            .anyMatch(notizJsonSpec -> notizKind1.getId().getId().equals(notizJsonSpec.getId()))
            .anyMatch(notizJsonSpec -> notizKind2.getId().getId().equals(notizJsonSpec.getId()));
    }

    @Test
    @TestAsAdminBS
    void findAllNotizenForDossier() {
        // ARRANGE
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Test Notiz")
                .withBemerkungen("Test Bemerkungen")
                .withStatus(NotizStatus.AKTIV)
                .withDossier(dossierService.getById(dossierId))
                .withVerantwortlicher(verantwortlicher)
                .withLabels(new LinkedHashSet<>()));
        notizService.persist(notiz);

        // ACT
        NotizListJsonSpec result = api
            .kinderNotizResourceFindAllForKind()
            .kindIdPath(kindId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizListJsonSpec.class);

        // ASSERT
        assertThat(result.getNotizen())
            .isNotEmpty()
            .anyMatch(notizJsonSpec -> notiz.getId().getId().equals(notizJsonSpec.getId())
                && "Test Notiz".equals(notizJsonSpec.getData().getTitel())
                && "Test Bemerkungen".equals(notizJsonSpec.getData().getBemerkungen()));
    }

    private List<LabelJsonSpec> getNonUniqueLabel() {

        var label = labelService.persist(LabelBuilder.create(
            builder -> builder
                .withRandomName("Unique Label 1")
                .withTyp(LabelTyp.NOTIZEN)));
        List<Label> labels = List.of(label, label, label);

        return labels.stream().map(LabelMapper::toJson)
            .map(LabelJsonSpecMapper.INSTANCE::labelJsonToLabelJsonSpec)
            .toList();

    }

    @Test
    @TestAsAdminBS
    void getNotiz() {
        // ARRANGE
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Einzelne Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossierService.getById(dossierId)));
        notizService.persist(notiz);

        // ACT
        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notiz.getId().getId());
        assertThat(result.getData().getTitel()).isEqualTo("Einzelne Notiz");
    }

    @Test
    @TestAsAdminBS
    void removeNotiz() {
        // ARRANGE
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Einzelne Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossierService.getById(dossierId)));
        notizService.persist(notiz);

        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);
        assertThat(result).isNotNull();

        // ACT
        api.notizResourceDeleteNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        notizService.findById(notiz.getId()).ifPresentOrElse(
            n -> fail("Notiz should have been deleted"),
            () -> {
                // nothing to check
            });
    }

    @Test
    @TestAsAdminBS
    void removeNotiz_ofInactiveKind_shouldThrow() {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();
        Dossier dossier = fixtureService.createDossier(kind);
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Einzelne Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossier));
        notizService.persist(notiz);

        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);
        assertThat(result).isNotNull();

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Notiz mit %s kann nicht vom Kind entfernt werden.",
            kind.getNameVorname(),
            kind.getId(),
            notiz.getId());

        // ACT
        AppValidationErrorJsonSpec validationError =  api
                .notizResourceDeleteNotiz()
                .notizIdPath(notiz.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotRemoveEntityFromInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }


    @Test
    @TestAsAdminBS
    void updateNotiz() throws IOException {
        // ARRANGE
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Base Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossierService.getById(dossierId)));
        notizService.persist(notiz);

        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        NotizSaveJsonSpec data = result.getData();
        List<LabelJsonSpec> labelsToUpdate = getNonUniqueLabel();
        data.setBemerkungen("changed bemerkung");
        data.setTitel("changed titel");
        data.setStatus(NotizStatusSpec.ABGESCHLOSSEN);
        data.setLabels(labelsToUpdate);

        String saveNotizJsonString = objectMapper.writeValueAsString(data);

        // ACT
        var notizUpdated = api.notizResourceUpdateNotiz()
            .notizIdPath(notiz.getId().getId())
            .reqSpec(spec -> {
                spec.setContentType("multipart/form-data");
                spec.addMultiPart("notizSaveJson", saveNotizJsonString, "application/json");
            })
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode()).extract().as(NotizJsonSpec.class);

        // ASSERT
        assertThat(notizUpdated).isNotNull();
        assertThat(notizUpdated.getData().getTitel()).isEqualTo("changed titel");
        assertThat(notizUpdated.getData().getBemerkungen()).isEqualTo("changed bemerkung");
        assertThat(notizUpdated.getData().getStatus()).isEqualTo(NotizStatusSpec.ABGESCHLOSSEN);
        assertThat(notizUpdated.getData().getLabels())
            .isNotEmpty()
            .hasSize(1);
    }

    @Test
    @TestAsAdminBS
    void updateNotizAllAtOnce() throws IOException {
        // ARRANGE
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Base Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossierService.getById(dossierId)));
        notizService.persist(notiz);

        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        NotizSaveJsonSpec data = result.getData();
        List<LabelJsonSpec> labelsToUpdate = getNonUniqueLabel();
        data.setBemerkungen("changed bemerkung with files");
        data.setTitel("changed titel with files");
        data.setStatus(NotizStatusSpec.ABGESCHLOSSEN);
        data.setLabels(labelsToUpdate);

        File testFile = TestUtil.loadTestImage();

        byte[] testFileBytes = Files.readAllBytes(TestUtil.loadTestWord().toPath());

        try {

            // Convert NotizSaveJsonSpec to JSON string
            String saveNotizJsonString = this.objectMapper.writeValueAsString(data);

            // ACT
            // Use the NotizenApiSpec method for updateNotizAllAtOnce
            NotizJsonSpec notizUpdated = api.notizResourceUpdateNotiz()
                .notizIdPath(notiz.getId().getId())
                .reqSpec(spec -> {
                    spec.addMultiPart("notizSaveJson", saveNotizJsonString, "application/json");
                })
                ._fileMultiPart(testFile)
                .reqSpec(spec -> {
                    spec.setContentType("multipart/form-data");
                    spec.addMultiPart("file3", "terstfile3name.docx", testFileBytes);
                })
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(NotizJsonSpec.class);

            // ASSERT
            assertThat(notizUpdated).isNotNull();
            assertThat(notizUpdated.getData().getTitel()).isEqualTo("changed titel with files");
            assertThat(notizUpdated.getData().getBemerkungen()).isEqualTo("changed bemerkung with files");
            assertThat(notizUpdated.getData().getStatus()).isEqualTo(NotizStatusSpec.ABGESCHLOSSEN);
            assertThat(notizUpdated.getData().getLabels())
                .isNotEmpty()
                .hasSize(1);
            assertThat(notizUpdated.getData().getFiles())
                .isNotEmpty()
                .hasSize(2);
        } catch (Exception e) {
            throw new RuntimeException("Error converting NotizSaveJsonSpec to JSON", e);
        }
    }


    @Test
    @TestAsAdminBS
    void updateNotizAllAtOnce_forInactiveKind_shouldThrow() throws IOException {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();
        Dossier dossier = fixtureService.createDossier(kind);
        Notiz notiz = NotizBuilder.create(
            builder -> builder
                .withID(ID.random(Notiz.class))
                .withTitel("Base Notiz")
                .withVerantwortlicher(verantwortlicher)
                .withDossier(dossier));
        notizService.persist(notiz);

        NotizJsonSpec result = api
            .notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(NotizJsonSpec.class);

        NotizSaveJsonSpec data = result.getData();
        List<LabelJsonSpec> labelsToUpdate = getNonUniqueLabel();
        data.setBemerkungen("changed bemerkung with files");
        data.setTitel("changed titel with files");
        data.setStatus(NotizStatusSpec.ABGESCHLOSSEN);
        data.setLabels(labelsToUpdate);

        File testFile = TestUtil.loadTestImage();

        byte[] testFileBytes = Files.readAllBytes(TestUtil.loadTestWord().toPath());


        // Convert NotizSaveJsonSpec to JSON string
        String saveNotizJsonString = this.objectMapper.writeValueAsString(data);
        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen an Notiz %s vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId(),
            notiz.getId());

        // ACT
        // Use the NotizenApiSpec method for updateNotizAllAtOnce
        AppValidationErrorJsonSpec validationError =  api.notizResourceUpdateNotiz()
                .notizIdPath(notiz.getId().getId())
                .reqSpec(spec -> {
                    spec.addMultiPart("notizSaveJson", saveNotizJsonString, "application/json");
                })
                ._fileMultiPart(testFile)
                .reqSpec(spec -> {
                    spec.setContentType("multipart/form-data");
                    spec.addMultiPart("file3", "terstfile3name.docx", testFileBytes);
                })
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditEntityForInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void getNotiz_errors_whenNotizNotFound() {
        // ACT & ASSERT
        var failure = api.notizResourceGetNotiz()
            .notizIdPath(ID.random(Notiz.class).getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract()
            .as(AppFailureErrorJsonSpec.class);
        assertThat(failure).isNotNull();
        assertThat(failure.getType().name()).isEqualTo(FailureType.FAILURE.name());
    }

    @Nested
    class FileEndpoints {

        @Test
        @TestAsAdminBS
        void uploadFileToNotiz() {
            // ARRANGE
            Notiz notiz = NotizBuilder.create(
                builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withTitel("Base Notiz")
                    .withVerantwortlicher(verantwortlicher)
                    .withDossier(dossierService.getById(dossierId)));
            notizService.persist(notiz);

            File pdfFile = TestUtil.load15mbImgFile();

            LocalDate today = getAppClock().localDateTime().now().toLocalDate();
            String formattedDate = FileNameUtil.DATE_FORMATTER_FOR_FILENAME.format(today);
            String formattedName = notiz.getDossier().getKind().getNameVorname().replace(' ', '-');

            String expectedFileName = formattedDate + "_" + formattedName + ".jpg";

            //ACT
            NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
                .notizIdPath(notiz.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(NotizJsonSpec.class);
            // ASSERT
            assertThat(notizJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
            assertThat(notizJsonSpec.getData().getFiles().getFirst().getCalculatedFileName())
                .isEqualTo(expectedFileName);
        }

        @ParameterizedTest
        @CsvSource({
            "/FileUtilTest/jpgDisguisedAsPng.png"
        })
        @TestAsAdminBS
        void given_filetype_not_matching_extension__uploadFileToNotiz__should_fail(String problematicFile) {
            // ARRANGE
            Notiz notiz = NotizBuilder.create(
                builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withTitel("Base Notiz")
                    .withVerantwortlicher(verantwortlicher)
                    .withDossier(dossierService.getById(dossierId)));
            notizService.persist(notiz);

            File pdfFile = TestUtil.loadTestfile(problematicFile);

            //ACT
            AppValidationErrorJsonSpec error = api.notizResourceAddNotizFile()
                .notizIdPath(notiz.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract()
                .as(AppValidationErrorJsonSpec.class);
            // ASSERT
            ViolationJsonSpec violation = error.getViolations().getFirst();
            assertThat(violation.getMessage()).contains(
                "png' does not match content type: 'image/jpeg'. Expected due to file extension: 'image/png'");
        }

        @ParameterizedTest
        @CsvSource({
            "/FileUtilTest/hello.sh",
        })
        @TestAsAdminBS
        void given_disallowed_file_type__uploadFileToNotiz__should_fail(String problematicFile) {
            // ARRANGE
            Notiz notiz = NotizBuilder.create(
                builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withTitel("Base Notiz")
                    .withVerantwortlicher(verantwortlicher)
                    .withDossier(dossierService.getById(dossierId)));
            notizService.persist(notiz);

            File pdfFile = TestUtil.loadTestfile(problematicFile);

            //ACT
            var violation = api.notizResourceAddNotizFile()
                .notizIdPath(notiz.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract()
                .as(AppValidationErrorJsonSpec.class);
            // ASSERT
            assertThat(violation.getViolations())
                .containsExactlyInAnyOrder(
                    new ViolationJsonSpec(
                        "",
                        "UploadInvalidFiletype",
                        "Dieser Datei-Typ kann nicht hochgeladen werden: application/octet-stream")
                );
        }

        @Test
        @TestAsAdminBS
        void downloadNotizFile() {
            // ARRANGe
            Notiz storedNotiz = fixtureService.createNotiz();

            File testfile = TestUtil.loadTestImage();
            NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
                .notizIdPath(storedNotiz.getId().getId())
                ._fileMultiPart(testfile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(NotizJsonSpec.class);

            assertThat(notizJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
            // ACT

            byte[] image = api.notizResourceDownloadNotizFile()
                .notizIdPath(storedNotiz.getId().getId())
                .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseOptions::andReturn)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .asByteArray();

            // ASSERT
            assertThat(image).isNotNull();
            assertThat(testfile)
                .hasSize(image.length)
                // check that loading the file synchronously works as well and returns the same content
                .hasSize(fileStoreService.getFileBytesSync(notizJsonSpec.getData()
                    .getFiles()
                    .getFirst()
                    .getFileIdInBucket()).length);
        }

        @Test
        @TestAsAdminBS
        void downloadNotizFileSync() {
            // ARRANGe
            Notiz storedNotiz = fixtureService.createNotiz();

            File testfile = TestUtil.loadTestImage();
            String name = testfile.getName();
            NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
                .notizIdPath(storedNotiz.getId().getId())
                ._fileMultiPart(testfile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(NotizJsonSpec.class);

            assertThat(notizJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
            // ACT

            byte[] image = api.notizResourceDownloadNotizFileSync()
                .notizIdPath(storedNotiz.getId().getId())
                .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseOptions::andReturn)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .header("Content-Disposition", "attachment;filename=\"" + name+ '"')
                .extract()
                .asByteArray();

            // ASSERT
            assertThat(image).isNotNull();
            assertThat(testfile)
                .hasSize(image.length)
                // check that loading the file synchronously works as well and returns the same content
                .hasSize(fileStoreService.getFileBytesSync(notizJsonSpec.getData()
                    .getFiles()
                    .getFirst()
                    .getFileIdInBucket()).length);
        }

        @Test
        @TestAsAdminBS
        void removeFileFromNotiz() {
            // ARRANGE
            Notiz notiz = NotizBuilder.create(
                builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withTitel("Base Notiz")
                    .withVerantwortlicher(verantwortlicher)
                    .withDossier(dossierService.getById(dossierId)));
            notizService.persist(notiz);

            File pdfFile = TestUtil.loadTestPDF();

            NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
                .notizIdPath(notiz.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(NotizJsonSpec.class);

            assertThat(notizJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);

            //ACT
            NotizJsonSpec notizAfterRemoval = api.notizResourceRemoveNotizFile()
                .notizIdPath(notiz.getId().getId())
                .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(NotizJsonSpec.class);

            // ASSERT
            assertThat(notizAfterRemoval.getData().getFiles())
                .isEmpty();
        }
    }
}
