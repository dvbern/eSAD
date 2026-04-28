package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.DocxAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BriefvorlageBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaSuperBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsOrthoptistBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.BriefvorlageApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppLanguagesSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BriefvorlageJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BriefvorlageListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BriefvorlageTypeSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.TestUtil.loadTestfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class BriefvorlageResourceITest extends AbstractITest {

    private final BriefvorlageApiSpec api = BriefvorlageApiSpec.briefvorlage(RequestSpecUtil.quarkusSpec());
    private final BriefvorlageService briefvorlageService;
    private final FixtureService fixtureService;
    private final ObjectMapper objectMapper;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.setTime(OffsetDateTime.of(
            2025, 5, 20,
            12, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    @TestAsAdminBS
    void findAllBriefvorlagen() {
        //ARRANGE
        Briefvorlage briefvorlage = BriefvorlageBuilder.create(builder -> builder
            .withName("Test Briefvorlage Name"));

        briefvorlageService.persist(briefvorlage);

        //ACT
        BriefvorlageListJsonSpec result = api.briefvorlageResourceFindAllBriefvorlagen()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(BriefvorlageListJsonSpec.class);

        //ASSERT
        assertThat(result.getBriefvorlageList()).isNotEmpty()
            .anyMatch(briefvorlageJsonSpec ->
                briefvorlage.getId().getId().equals(briefvorlageJsonSpec.getId())
                    && briefvorlage.getName().equals(briefvorlageJsonSpec.getData().getName()));
    }

    @Test
    @TestAsSupportBS
    void createBriefvorlage() throws JsonProcessingException {
        // ARRANGE
        File wordFile = TestUtil.loadTestWord();
        BriefvorlageSaveJson saveJson =
            new BriefvorlageSaveJson("Meine vorlage", BriefvorlageType.ANLASS, AppLanguages.DE, 0L);
        String s = objectMapper.writeValueAsString(saveJson);

        // ACT
        BriefvorlageJsonSpec result = api.briefvorlageResourceCreateBriefvorlage()
            ._fileMultiPart(wordFile)
            .briefvorlageSaveJsonForm(s)
            .execute(Function.identity())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void updateBriefvorlage() throws JsonProcessingException {
        // ARRANGE
        FileData fileData = FileDataBuilder.defaultWordFileData();
        Briefvorlage briefvorlage = BriefvorlageBuilder.create(builder -> builder
            .withName("Test Briefvorlage Name")
            .withFile(fileData));

        Briefvorlage originalBriefvorlage = briefvorlageService.persist(briefvorlage);
        BriefvorlageSaveJson updateSaveJson =
            new BriefvorlageSaveJson("Neuer name!", originalBriefvorlage.getType(), originalBriefvorlage.getSprache(), 0L);
        File newFile = TestUtil.loadTestWord();

        String updateSaveJsonString = objectMapper.writeValueAsString(updateSaveJson);
        // ACT
        BriefvorlageJsonSpec result = api.briefvorlageResourceUpdateBriefvorlage()
            ._fileMultiPart(newFile)
            .briefvorlageIdPath(originalBriefvorlage.getId().getId())
            .briefvorlageSaveJsonForm(updateSaveJsonString)
            .execute(Function.identity())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getName()).isEqualTo("Neuer name!");
        assertThat(result.getData().getType()).isEqualTo(BriefvorlageTypeSpec.KIND);
        assertThat(result.getData().getSprache()).isEqualTo(AppLanguagesSpec.DE);
        assertThat(result.getFileData().getFileName().getFileName()).isEqualTo(newFile.getName());
    }

    @Test
    @TestAsSupportBS
    void deleteBriefvorlage() {
        // ARRANGE
        Briefvorlage briefvorlage1 = BriefvorlageBuilder.create(builder -> builder
            .withName("Briefvorlage1"));
        Briefvorlage persist1 = briefvorlageService.persist(briefvorlage1);

        Briefvorlage briefvorlage2 = BriefvorlageBuilder.create(builder -> builder
            .withName("Briefvorlage2"));
        Briefvorlage persist2 = briefvorlageService.persist(briefvorlage2);

        // ACT
        api.briefvorlageResourceRemoveBriefvorlage()
            .briefvorlageIdPath(persist2.getId().getId())
            .execute(Function.identity())
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        ID<Briefvorlage> id = persist2.getId();
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> briefvorlageService.getById(id)
        );
        assertThat(ex).hasMessageContaining("ENTITY_NOT_FOUND");

        Briefvorlage value1 = briefvorlageService.getById(persist1.getId());
        assertThat(value1).isNotNull();
    }

    @Test
    @TestAsMpaSuperBS
    void updateBriefvorlagen_should_work_for_user_with_super_berechtigung() throws JsonProcessingException {
        // ARRANGE
        FileData fileData = FileDataBuilder.defaultWordFileData();
        Briefvorlage briefvorlage = BriefvorlageBuilder.create(builder -> builder
            .withName("Test Briefvorlage Name")
            .withFile(fileData));

        Briefvorlage originalBriefvorlage = briefvorlageService.persist(briefvorlage);
        BriefvorlageSaveJson updateSaveJson =
            new BriefvorlageSaveJson(
                "Neuer name can be set by superuser!",
                originalBriefvorlage.getType(),
                originalBriefvorlage.getSprache(),
                0L);
        File newFile = TestUtil.loadTestWord();

        String updateSaveJsonString = objectMapper.writeValueAsString(updateSaveJson);
        // ACT
        BriefvorlageJsonSpec result = api.briefvorlageResourceUpdateBriefvorlage()
            ._fileMultiPart(newFile)
            .briefvorlageIdPath(originalBriefvorlage.getId().getId())
            .briefvorlageSaveJsonForm(updateSaveJsonString)
            .execute(Function.identity())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getName()).isEqualTo("Neuer name can be set by superuser!");
        assertThat(result.getData().getType()).isEqualTo(BriefvorlageTypeSpec.KIND);
        assertThat(result.getData().getSprache()).isEqualTo(AppLanguagesSpec.DE);
        assertThat(result.getFileData().getFileName().getFileName()).isEqualTo(newFile.getName());
    }

    @Test
    @TestAsOrthoptistBS
    void updateBriefvorlage_should_not_work_for_orthoptistin() throws JsonProcessingException {

        // ARRANGE
        FileData fileData = FileDataBuilder.defaultWordFileData();
        Briefvorlage briefvorlage = BriefvorlageBuilder.create(builder -> builder
            .withName("Test Briefvorlage updatetest")
            .withFile(fileData));

        Briefvorlage originalBriefvorlage = briefvorlageService.persist(briefvorlage);
        BriefvorlageSaveJson updateSaveJson =
            new BriefvorlageSaveJson(
                "this should not work!",
                originalBriefvorlage.getType(),
                originalBriefvorlage.getSprache(),
                0L);
        File newFile = TestUtil.loadTestWord();

        String updateSaveJsonString = objectMapper.writeValueAsString(updateSaveJson);
        // ACT
        api.briefvorlageResourceUpdateBriefvorlage()
            ._fileMultiPart(newFile)
            .briefvorlageIdPath(originalBriefvorlage.getId().getId())
            .briefvorlageSaveJsonForm(updateSaveJsonString)
            .execute(Function.identity())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());

    }

    @Test
    @TestAsSupportBS
    void downloadBrief_for_anlass() throws JsonProcessingException {
        // ARRANGE
        File briefvorlageFile = TestUtil.loadBriefvorlageFile(BriefvorlageType.ANLASS);
        BriefvorlageSaveJson saveJson =
            new BriefvorlageSaveJson("testbrief", BriefvorlageType.ANLASS, AppLanguages.DE, 0L);
        String s = objectMapper.writeValueAsString(saveJson);

        BriefvorlageJsonSpec briefvorlageJsonSpec = api.briefvorlageResourceCreateBriefvorlage()
            ._fileMultiPart(briefvorlageFile)
            .briefvorlageSaveJsonForm(s)
            .execute(Function.identity())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        Einladung einladung = fixtureService.createEinladung();

        // ACT
        var success = api.briefvorlageResourceDownloadBrief()
            .briefvorlageIdPath(briefvorlageJsonSpec.getId())
            .kindIdPath(einladung.getDossier().getKind().getId().getId())
            .einladungIdQuery(einladung.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();

        // ASSERT
        Assertions.assertThat(success).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadBrief_for_kind() {
        // ARRANGE
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        Einladung einladung = fixtureService.createEinladung();
        // ACT
        var success = api.briefvorlageResourceDownloadBrief()
            .briefvorlageIdPath(briefvorlage.getId().getId())
            .kindIdPath(einladung.getDossier().getKind().getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();
        // ASSERT
        Assertions.assertThat(success).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadBriefvorlageFile() throws JsonProcessingException {
        // ARRANGE
        File wordFile = TestUtil.loadTestWord();
        BriefvorlageSaveJson saveJson =
            new BriefvorlageSaveJson("AnlassVorlage", BriefvorlageType.ANLASS, AppLanguages.DE, 0L);
        String s = objectMapper.writeValueAsString(saveJson);

        BriefvorlageJsonSpec briefvorlage = api.briefvorlageResourceCreateBriefvorlage()
            ._fileMultiPart(wordFile)
            .briefvorlageSaveJsonForm(s)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        // ACT
        String result = api.briefvorlageResourceDownloadBriefvorlageFile()
            .briefvorlageIdPath(briefvorlage.getId())
            .execute(ResponseOptions::andReturn)
            .then().statusCode(Status.OK.getStatusCode())
            .extract()
            .asString();

        // ASSERT
        Assertions.assertThat(result).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadTestBriefe() throws IOException {
        // ARRANGE
        File briefvorlageAnlassFile = TestUtil.loadBriefvorlageFile(BriefvorlageType.ANLASS);
        BriefvorlageSaveJson briefvorlageAnlass =
            new BriefvorlageSaveJson("anlass-testbrief", BriefvorlageType.ANLASS, AppLanguages.DE, 0L);
        String briefvolageAnlassString = objectMapper.writeValueAsString(briefvorlageAnlass);

        File briefvorlageKindFile = TestUtil.loadBriefvorlageFile(BriefvorlageType.KIND);
        BriefvorlageSaveJson briefvorlageKind =
            new BriefvorlageSaveJson("kind-testbrief", BriefvorlageType.KIND, AppLanguages.DE, 0L);
        String briefvorlageKindString = objectMapper.writeValueAsString(briefvorlageKind);

        BriefvorlageJsonSpec briefvorlageAnlassResponse = api.briefvorlageResourceCreateBriefvorlage()
            ._fileMultiPart(briefvorlageAnlassFile)
            .briefvorlageSaveJsonForm(briefvolageAnlassString)
            .execute(Function.identity())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        BriefvorlageJsonSpec briefvorlageKindResponse = api.briefvorlageResourceCreateBriefvorlage()
            ._fileMultiPart(briefvorlageKindFile)
            .briefvorlageSaveJsonForm(briefvorlageKindString)
            .execute(Function.identity())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(BriefvorlageJsonSpec.class);

        // ACT
        var successAnlass = api.briefvorlageResourceDownloadTestBrief()
            .briefvorlageIdPath(briefvorlageAnlassResponse.getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        var successKind = api.briefvorlageResourceDownloadTestBrief()
            .briefvorlageIdPath(briefvorlageKindResponse.getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        Assertions.assertThat(successAnlass).isNotNull();

        File expectedAnlassCntFile = loadTestfile("/DocMergerTest/expected/all-properties-textcontent.txt");
        String expectedTextAnlass = FileUtil.readTextFromPath(expectedAnlassCntFile.toPath());
        DocxAssert.assertThatDocx(new ByteArrayInputStream(successAnlass))
            .containsExactly(expectedTextAnlass);

        File expectedKindCntFile = loadTestfile("/DocMergerTest/expected/kind-properties-textcontent.txt");
        String expectedTextKind = FileUtil.readTextFromPath(expectedKindCntFile.toPath());
        Assertions.assertThat(successKind).isNotNull();
        DocxAssert.assertThatDocx(new ByteArrayInputStream(successKind))
            .containsExactly(expectedTextKind);
    }
}
