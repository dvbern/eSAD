package ch.dvbern.oss.hemed.esc.api.features.printer;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageSaveJson;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfang;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BriefvorlageJsonSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PrinterApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KinderdossierPrintVariantSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@QuarkusTest
@RequiredArgsConstructor
class PrinterResourceITest extends AbstractITest {

    private final PrinterApiSpec api = PrinterApiSpec.printer(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizenApi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;

    @Test
    @TestAsSupport
    void downloadKinderdossier() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Kind kind = einladung.getDossier().getKind();

        String fileName = URLEncoder.encode(
            "Schulärztliches_Dossier_von_"
                + kind.getName()
                + '-'
                + kind.getVorname()
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.of("de", "CH")))
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadKinderdossier()
            .kindIdPath(kind.getId().getId())
            .variationQuery(KinderdossierPrintVariantSpec.GESAMTES)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename*=UTF-8''" + fileName)
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsSupport
    void downloadKinderdossierForAnlass() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Kind kind = einladung.getDossier().getKind();
        String fileName = URLEncoder.encode(
            "Schulärztliches_Dossier_von_"
                + kind.getName()
                + '-'
                + kind.getVorname()
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.of("de", "CH")))
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadKinderdossierForAnlass()
            .kindIdPath(kind.getId().getId())
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename*=UTF-8''" + fileName)
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsEmpfang
    void downloadKinderdossierForAnlass_should_fail_for_Empfang() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Kind kind = einladung.getDossier().getKind();
        // ACT
        api.printerResourceDownloadKinderdossierForAnlass()
            .kindIdPath(kind.getId().getId())
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.FORBIDDEN.getStatusCode())
            .extract();
    }

    @Test
    @TestAsSupport
    void downloadKinderdossierWithNotizeListInZipFile() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Kind kind = einladung.getDossier().getKind();
        Notiz notiz = fixtureService.createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_FRAGEBOGEN));
        File pdfFile = TestUtil.loadTestPDF();

        notizenApi.notizResourceAddNotizFile()
            .kindIdPath(kind.getId().getId())
            .notizIdPath(notiz.getId().getId())
            ._fileMultiPart(pdfFile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode());

        String fileName = URLEncoder.encode(
            "Schulärztliches_Dossier_von_"
                + kind.getName()
                + '-'
                + kind.getVorname()
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.of("de", "CH")))
                + ".zip", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadKinderdossier()
            .kindIdPath(kind.getId().getId())
            .variationQuery(KinderdossierPrintVariantSpec.GESAMTES)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename*=UTF-8''" + fileName)
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        fixtureService.createEinladung(anlass);

        String fileName = URLEncoder.encode(
            "Anlassexport_"
                + "Test-getAnlass"
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(DATE_FORMATTER_FOR_FILENAME)
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename*=UTF-8''" + fileName)
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }
}
