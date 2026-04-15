package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.UUID;
import java.util.function.Function;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArchivdokumentBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ArchivdokumentApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArchivdokumentListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
class ArchivdokumentResourceITest extends AbstractITest {

    private final ArchivdokumentApiSpec api = ArchivdokumentApiSpec.archivdokument(RequestSpecUtil.quarkusSpec());
    private final FixtureService fixtureService;
    private final ArchivdokumentService archivdokumentService;
    private final BenutzerService benutzerService;
    private final TenantProducer tenantProducer;

    @Test
    @TestAsSupportBE
    @Order(10)
    void findAllOffeneArchivdokument_as_Support_find_all_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument_offen_I = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_I);

        Archivdokument archivdokument_offen_II = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_II);

        Archivdokument archivdokument_abgeschlossen = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_abgeschlossen);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .onlyOpenQuery(true)
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokument_offen_II.getId().getId().equals(resultItem.getId()))
            .anyMatch(resultItem -> archivdokument_offen_I.getId().getId().equals(resultItem.getId()));
        assertThat(result.getArchivdokumentList()).hasSize(2);
    }

    @Test
    @TestAsAdminBE
    @Order(20)
    void findAllAbgeschlosseneArchivdokument_finds_own_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        var adminId = ID.of(UUID.fromString(TestAsAdminBE.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokument.getId().getId().equals(resultItem.getId()));
    }

    @Test
    @TestAsAdminBE
    @Order(30)
    void findAllOffeneArchivdokument_finds_own_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        var adminId = ID.of(UUID.fromString(TestAsAdminBE.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument archivdokument_offen_I = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_I);

        Archivdokument archivdokument_offen_II = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_II);

        Archivdokument archivdokument_abgeschlossen = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_abgeschlossen);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .onlyOpenQuery(true)
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokument_offen_I.getId().getId().equals(resultItem.getId()));
        assertThat(result.getArchivdokumentList()).hasSize(1);
    }

    @Test
    @TestAsSupportBE
    @Order(40)
    void downloadArchivdokument_works_and_sets_status_and_timestamtp() {
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withArchivierungTyp(ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        assertThat(archivdokument.getArchivierungStatus()).isEqualTo(ArchivierungStatus.OFFEN);
        assertThat(archivdokument.getTimestampDownloaded()).isNull();

        // ACT
        var success = api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();

        // ASSERT
        Assertions.assertThat(success).isNotNull();

        Archivdokument archivDokumentAfterDownload = archivdokumentService.getById(archivdokument.getId());

        assertThat(archivDokumentAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED);
        assertThat(archivDokumentAfterDownload.getTimestampDownloaded()).isNotNull();
    }

    @Test
    @TestAsSupportBE
    @Order(50)
    void downloadArchivdokument_works_and_sets_status_for_kind_deaktiviert() {
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        assertThat(archivdokument.getArchivierungStatus()).isEqualTo(ArchivierungStatus.OFFEN);
        assertThat(archivdokument.getTimestampDownloaded()).isNull();

        // ACT
        api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();

        // ASSERT
        Archivdokument archivDokumentAfterDownload = archivdokumentService.getById(archivdokument.getId());
        assertThat(archivDokumentAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
    }

    @Test
    @TestAsAdminBE
    @Order(60)
    void downloadArchivdokument_throws_if_owner_is_not_current_benutzer() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        //ACT
        AppFailureErrorJsonSpec failure = api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(failure).isNotNull();
        assertThat(failure.getSummary()).contains("hat keine Berechtigung für Entity");
    }
}
