package ch.dvbern.oss.hemed.esc.api.features.printer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfangBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PrinterApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassPrintVariantSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KinderdossierPrintVariantSpec;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
class PrinterResourceITest extends AbstractITest {

    private final PrinterApiSpec api = PrinterApiSpec.printer(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizenApi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final TenantSettingsService tenantSettingsService;

    @Test
    @TestAsSupportBS
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
            .variantQuery(KinderdossierPrintVariantSpec.GESAMTES)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsSupportBS
    void downloadKinderdossier_withTabInName() {
        // ARRANGE
        String vorname = "Max";
        String name = "Musterkind";
        Kind kind = fixtureService.createKind(
            vorname + "\tT\u00A0", // Tab and Non-Breaking Space
            name + "\nN\u2028", // New Line and Line Separator
            LocalDate.of(2010, 2, 12),
            Geschlecht.MAENNLICH
        );
        fixtureService.createEinladung(kind);

        String fileName = URLEncoder.encode(
            "Schulärztliches_Dossier_von_"
                + name
                + "-N--"
                + vorname
                + "-T-_"
                + getAppClock().localDateTime().now().toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.of("de", "CH")))
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadKinderdossier()
            .kindIdPath(kind.getId().getId())
            .variantQuery(KinderdossierPrintVariantSpec.GESAMTES)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsSupportBS
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
                "attachment;filename=\"" + fileName + '\"')
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsEmpfangBS
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
    @TestAsSupportBS
    void downloadKinderdossierWithNotizeListInZipFile() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Kind kind = einladung.getDossier().getKind();
        Notiz notiz =
            fixtureService.createNotiz(einladung.getDossier(), Set.of(Label.ID_LABEL_FRAGEBOGEN), NotizTyp.FRAGEBOGEN);
        File pdfFile = TestUtil.loadTestPDF();

        notizenApi.notizResourceAddNotizFile()
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
            .variantQuery(KinderdossierPrintVariantSpec.GESAMTES)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename=\"" + fileName + '"')
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
                + "2022-23-SU-4.PS-Test-getAnlass"
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(DATE_FORMATTER_FOR_FILENAME)
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .variantQuery(AnlassPrintVariantSpec.ALLGEMEIN)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile_withoutTitle() {
        // ARRANGE
        TenantSettingsService tenantSettingsServiceMock = mock(TenantSettingsService.class);
        when(tenantSettingsServiceMock.isAnlassTitleRequired()).thenReturn(false);
        when(tenantSettingsServiceMock.isZentraleOrganisation()).thenReturn(true);
        when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSettingsService.getTenantSetting());
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);

        Anlass anlass = fixtureService.createAnlassWithoutTitle(
            AnlassTyp.KLASSE_4,
            LocalDate.of(2023, 5, 5));
        fixtureService.createEinladung(anlass);

        String fileName = URLEncoder.encode(
            "Anlassexport_"
                + "2022-23-SU-4.PS"
                + '_'
                + getAppClock().localDateTime().now().toLocalDate().format(DATE_FORMATTER_FOR_FILENAME)
                + ".pdf", StandardCharsets.UTF_8);

        // ACT
        byte[] content = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .variantQuery(AnlassPrintVariantSpec.ALLGEMEIN)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header(
                "Content-Disposition",
                "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();
        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile_with_variant_IMPFEN_prints_geplante_Impfungen() throws IOException {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        UntersuchungImpfungen untersuchungImpfungen = Objects.requireNonNull(untersuchung.getUntersuchungImpfungen());
        UntersuchungImpfungIntern geplanteImpfung =
            fixtureService.createUntersuchungImpfungIntern(untersuchungImpfungen);

        // ACT
        byte[] content = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .variantQuery(AnlassPrintVariantSpec.IMPFEN)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT: the table of the geplante Impfungen replaces the list of the eingeladene Kinder
        // and lists the planned Impfstoff by its name
        Assertions.assertThat(extractPdfText(content))
            .contains("Geplante Impfungen")
            .contains(geplanteImpfung.getImpfstoff().getName())
            .doesNotContain("Klassenstufe");
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile_with_variant_IMPFEN_returns_400_if_disabled_in_AnlassConfig() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithShowAnlassPrintImpfen(false);

        // ACT & ASSERT
        api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .variantQuery(AnlassPrintVariantSpec.IMPFEN)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .body(containsString("AnlassPrintVariantDisabled"));
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile_with_variant_ALLGEMEIN_is_not_affected_by_showAnlassPrintImpfen() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithShowAnlassPrintImpfen(false);

        // ACT
        byte[] content = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .variantQuery(AnlassPrintVariantSpec.ALLGEMEIN)
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        Assertions.assertThat(content).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsAdminBS
    void downloadAnlassExportFile_returns_400_without_variant() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();

        // ACT
        AppValidationErrorJsonSpec validationError = api.printerResourceDownloadAnlassPrint()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        // ASSERT: variant is required, as in the OpenAPI schema, instead of silently falling back to ALLGEMEIN
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(validationError)
            .extractingExactlyOneViolation()
            .hasClientKey("NotNull");
    }

    @NonNull
    private static String extractPdfText(byte[] pdfBytes) throws IOException {
        StringBuilder text = new StringBuilder();
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(page)));
                text.append('\n');
            }
        }
        return text.toString();
    }
}
