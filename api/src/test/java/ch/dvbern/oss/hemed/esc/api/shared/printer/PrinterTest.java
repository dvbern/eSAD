package ch.dvbern.oss.hemed.esc.api.shared.printer;

import ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallFangen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallWerfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Hampelmann;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anlass.AnlassPlanungPrintDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anlass.AnlassPlanungPrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer.AnonymerEintragPrintDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer.AnonymerEintragPrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.AnlassUntersuchungPrintDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierUntersuchungImpfungenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrinter;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonKindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBemerkungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungDurchfuehrungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungLaeusekontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSehenBeurteilungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungTriageBuilder;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings("TestMethodWithoutAssertion")
@Slf4j
class PrinterTest {

    private static final String OUTPUT_PATH = Paths.get(FileUtils.getTempDirectoryPath(), "esc", "pdf").toString();

    private final FileStoreService fileStoreServiceMock = Mockito.mock(FileStoreService.class);
    private final TenantSettingsService tenantSettingsServiceMock = Mockito.mock(TenantSettingsService.class);
    private final ConfigService configServiceMock = Mockito.mock(ConfigService.class);

    @BeforeAll
    static void setUp() throws IOException {
        FileUtils.forceMkdir(new File(OUTPUT_PATH));
        for (TenantEnum tenant : TenantEnum.values()) {
            FileUtils.forceMkdir(new File(OUTPUT_PATH + '/' + tenant));
        }
    }

    @BeforeEach
    void setUpBeforeEach() {
        Mockito.when(tenantSettingsServiceMock.getTenantSetting())
            .thenReturn(TenantSettingBuilder.defaultTenantSetting());
        Mockito.when(tenantSettingsServiceMock.isTenantHeaderInExportsEnabled()).thenReturn(true);
    }

    @Test
    void addHeader_showHeaderTrue_rendersTenantAddress() throws IOException {
        // ARRANGE & ACT
        byte[] pdfBytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, TenantEnum.BE, fileStoreServiceMock);
            Document document = basePrinter.getDocument(byteArrayOutputStream);
            basePrinter.addHeader(document, true);
            document.close();
            pdfBytes = byteArrayOutputStream.toByteArray();
        }

        // ASSERT
        assertThat(extractPdfText(pdfBytes)).contains("Bern");
    }

    @Test
    void addHeader_showHeaderFalse_omitsTenantAddress() throws IOException {
        // ARRANGE & ACT
        byte[] pdfBytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, TenantEnum.BE, fileStoreServiceMock);
            Document document = basePrinter.getDocument(byteArrayOutputStream);
            basePrinter.addHeader(document, false);
            document.close();
            pdfBytes = byteArrayOutputStream.toByteArray();
        }

        // ASSERT
        assertThat(extractPdfText(pdfBytes)).doesNotContain("Bern");
    }

    @Test
    void printTableImpfstatus_sortsDiseasesByKrankheitEnumOrder() throws IOException {
        // ARRANGE: diseases provided in a deliberately unsorted order
        List<UntersuchungImpfstatusKrankheit> unsorted = List.of(
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.POLIO)),
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.DIPHTERIE)),
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.PERTUSSIS)),
            UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.forKrankheit(Krankheit.TETANUS)));

        // ACT
        byte[] pdfBytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, TenantEnum.BS, fileStoreServiceMock);
            Document document = basePrinter.getDocument(byteArrayOutputStream);
            KinderdossierUntersuchungImpfungenUtil.printTableImpfstatus(unsorted, basePrinter, document);
            document.close();
            pdfBytes = byteArrayOutputStream.toByteArray();
        }

        // ASSERT: rendered in Krankheit enum declaration order DIPHTERIE, TETANUS, PERTUSSIS, POLIO
        String pdfText = extractPdfText(pdfBytes);
        int diphterie = pdfText.indexOf("Diphtherie");
        int tetanus = pdfText.indexOf("Tetanus");
        int pertussis = pdfText.indexOf("Pertussis");
        int polio = pdfText.indexOf("Poliomyelitis");
        assertThat(diphterie).isGreaterThanOrEqualTo(0).isLessThan(tetanus);
        assertThat(tetanus).isLessThan(pertussis);
        assertThat(pertussis).isLessThan(polio);
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void full_kinderdossier_with_files(TenantEnum tenantEnum) {
        try (InputStream pdfInput = BasePrinter.class.getResourceAsStream("/FileUtilTest/pdfFileExample.pdf");
            InputStream pictureInput = BasePrinter.class.getResourceAsStream("/FileUtilTest/portrait.jpg");
            FileOutputStream outputStream = getFileOutputStream("Kinderdossier-full", tenantEnum)) {

            if (pdfInput != null && pictureInput != null) {
                Mockito.when(fileStoreServiceMock.getFileBytesSync("picture")).thenReturn(pictureInput.readAllBytes());
                Mockito.when(fileStoreServiceMock.getFileBytesSync("pdf")).thenReturn(pdfInput.readAllBytes());
            }

            Kind kind = KindBuilder.defaultKind();
            kind.setName(kind.getName() + "\twith tab");

            KontaktPersonKind kp2 = KontaktPersonKindBuilder.create(kontaktPersonKind -> {
                kontaktPersonKind.withName("Živan\tGarça", "Üveyşê");
                kontaktPersonKind.withBeziehung(KontaktPersonKindBeziehung.VATER);

            });
            List<KontaktPersonKind> kontaktPersonKind = List.of(
                KontaktPersonKindBuilder.defaultKontaktPersonKind(),
                kp2);

            List<FileData> files = List.of(
                FileDataBuilder.defaultImgFileData(),
                FileDataBuilder.create(builder -> builder.withMimeType(MediaType.parse(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    ).withFileName("FilenameWith\tTab.docx")
                ),
                FileDataBuilder.defaultPdfFileData()
            );

            List<Notiz> notizes = List.of(
                NotizBuilder.create(builder -> builder.withFiles(files)
                    .withBemerkungen(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur blandit ante. "
                            + "Pellentesque a urna felis. Donec finibus tempor augue, at tempor orci cursus quis. "
                            + "Nam ultricies risus neque, et laoreet lectus vulputate sed. Sed facilisis placerat "
                            + "semper. Proin justo libero, facilisis eu augue non, fermentum tempor tortor. Nunc "
                            + "luctus luctus tristique.")),
                NotizBuilder.create(builder -> builder
                    .withTimestampErstellt(OffsetDateTime.now().minusDays(4))
                    .withBemerkungen(
                        "Lange ist es heeeeer!"))
            );

            UntersuchungDurchfuehrung untersuchungDurchfuehrung =
                UntersuchungDurchfuehrungBuilder.create(builder ->
                    builder.isNachzuegler(true)
                        .isNachkontrolle(true)
                        .isNeueintritt(false));
            Untersuchung untersuchungLongAgo =
                UntersuchungBuilder.create(builder -> builder.withDurchgefuehrt(untersuchungDurchfuehrung));
            Anlass anlassLongAgo =
                AnlassBuilder.create(builder -> builder.withDatumSetSchuljahr(LocalDate.now().minusMonths(2)));
            UntersuchungImpfungIntern untersuchungImpfungIntern =
                UntersuchungImpfungInternBuilder.create(builder -> builder.withDatumImpfung(LocalDate.now()));
            AnlassUntersuchungPrintDTO anlassKeineTeilnahme = AnlassUntersuchungPrintDTO.ofEinladung(
                EinladungBuilder.create(builder -> builder.withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
                    .withAnlass(anlassLongAgo)
                    .withUntersuchung(untersuchungLongAgo)),
                List.of(untersuchungImpfungIntern),
                List.of(UntersuchungImpfstatusKrankheitBuilder.defaultUntersuchungImpfungen()),
                List.of());

            UntersuchungTriage untersuchungTriage =
                UntersuchungTriageBuilder.create(builder -> builder
                    .withTriageSu(true, "Das kommt schon gut :)"));
            Untersuchung untersuchung =
                UntersuchungBuilder.create(builder -> builder.withUntersuchungTriage(untersuchungTriage));
            Anlass anlassToday = AnlassBuilder.create(builder -> builder.withDatumSetSchuljahr(LocalDate.now()));
            anlassToday.setTitel(anlassToday.getTitel() + "\tTest");
            AnlassUntersuchungPrintDTO anlassTeilnahme = AnlassUntersuchungPrintDTO.ofEinladung(
                EinladungBuilder.create(builder ->
                    builder.withStatus(EinladungStatus.TEILNAHME_BESTAETIGT)
                        .withAnlass(anlassToday)
                        .withUntersuchung(untersuchung)),
                List.of(UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now())),
                List.of(),
                List.of());

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKind,
                List.of(anlassKeineTeilnahme, anlassTeilnahme),
                notizes,
                LocalDate.now(),
                TenantEnum.BS,
                AppLanguages.DE,
                KinderdossierPrintVariant.GESAMTES
            );

            KinderdossierPrinter.print(
                dto,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void anlaesse_kinderdossier(TenantEnum tenantEnum) {
        try (FileOutputStream outputStream = getFileOutputStream("Kinderdossier-anlaesse", tenantEnum)) {

            Kind kind = KindBuilder.defaultKind();
            List<KontaktPersonKind> kontaktPersonKind = List.of(
                KontaktPersonKindBuilder.defaultKontaktPersonKind(),
                KontaktPersonKindBuilder.defaultKontaktPersonKind());

            Anlass anlassLongAgo = AnlassBuilder.create(builder ->
                builder.withDatumSetSchuljahr(LocalDate.now().minusMonths(2))
                    .withArzt(BenutzerBuilder.defaultBenutzer())
                    .withMPA(BenutzerBuilder.defaultBenutzer())
                    .withWeiterePerson(BenutzerBuilder.defaultBenutzer()));

            Anlass anlassFolgeTermin = AnlassBuilder.create(builder ->
                builder
                    .withDatumSetSchuljahr(LocalDate.now().plusMonths(2))
                    .withTitle("Folgetermin Impfung")
                    .withAnlassConfig(AnlassConfigBuilder.create(anlassConfigBuilder -> builder.withAnlassTyp(AnlassTyp.IMPFTERMIN)))
            );
            assertThat(anlassFolgeTermin.getTitel()).isNotNull();
            AnlassWithDateTimeJson anlassWithDateTime = new AnlassWithDateTimeJson(
                anlassFolgeTermin.getId().getUUID(),
                anlassFolgeTermin.getAnlassConfig().getAnlassTyp(),
                anlassFolgeTermin.getTitel(),
                anlassFolgeTermin.getDatum(),
                anlassFolgeTermin.getZeitVonBis() != null ? anlassFolgeTermin.getZeitVonBis().getVon() : null,
                anlassFolgeTermin.getZeitVonBis() != null ? anlassFolgeTermin.getZeitVonBis().getBis() : null
            );

            AnlassUntersuchungPrintDTO anlassKeineTeilnahme = AnlassUntersuchungPrintDTO.ofEinladung(
                EinladungBuilder.create(builder -> builder.withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
                    .withAnlass(anlassLongAgo)),
                List.of(UntersuchungImpfungInternBuilder.defaultUntersuchungImpfungIntern(LocalDate.now())),
                List.of(UntersuchungImpfstatusKrankheitBuilder.defaultUntersuchungImpfungen()),
                List.of(anlassWithDateTime));

            UntersuchungTriage untersuchungTriage =
                UntersuchungTriageBuilder.create(builder -> builder
                    .withTriageSu(true, "Das kommt schon gut :)"));
            UntersuchungDurchfuehrung untersuchungDurchfuehrung =
                UntersuchungDurchfuehrungBuilder.create(builder ->
                    builder.isNachzuegler(true)
                        .isNachkontrolle(true)
                        .isNeueintritt(false)
                        .withBemerkung("Test Bemerkung bei Daten"));

            UntersuchungSehenBeurteilung untersuchungSehenBeurteilung =
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withBrilleLinsen(
                        BrilleLinsenResultat.MIT, null)
                    .withStereotest(StereotestResultat.NICHT_GEPRUEFT, null)
                    .withFarbsehen(FarbsehenResultat.PATHOLOGISCH, null)
                    .withVerwendeteTafel(VerwendetTafelResultat.BUCHSTABEN, null)
                    .withFernvisus(
                        List.of(DistanzSehenResultat.NICHT_GEPRUEFT, DistanzSehenResultat.RECHTS_REDUZIERT),
                        null)
                    .withFernvisusRechts(BigDecimal.valueOf(2.5))
                    .withNahvisus(List.of(DistanzSehenResultat.NICHT_GEPRUEFT), null)
                    .withMotilitaet(MotilitaetResultat.NORMAL, null)
                    .withStrabismus(List.of(StrabismusResultat.KEIN_STRABISMUS), null)
                    .withStrabismusAuspraegung(StrabismusAuspraegungResultat.LATENT, null)
                    .withStrabismusAuftreten(StrabismusAuftretenResultat.KEIN_STRABISMUS, null)
                    .withInspektion(InspektionKonvergenzResultat.AUFFAELLIG, null)
                    .withKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG, null)
                    .withBemerkung("Eine sehr ausführliche bemerkung!")
                );

            UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm = UntersuchungHoerenAudiogrammBuilder.create(
                builder -> builder
                    .withAudiogrammBeurteilung(
                        List.of(
                            AudiogrammBeurteilung.NICHT_GEPRUEFT,
                            AudiogrammBeurteilung.RECHTS_REDUZIERT), null)
                    .withAudiogrammRechts(Dezibel.DB50)
                    .withAudiogrammLinks(Dezibel.DB30)
                    .withZusaetzlicheAngaben(List.of(HoerenZusaetzlicheAngaben.ERKAELTUNG), null)
                    .withBemerkung("Eine weitere Audiogramm Bemerkung!")
            );
            UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie = UntersuchungHoerenOtoskopieBuilder.create(
                builder -> builder
                    .withOtoskopieRechts(
                        List.of(OtoskopieResultat.ANDERES, OtoskopieResultat.TROMMELFELL_CERUMEN),
                        null)
                    .withOtoskopieRechtsResultatAndere("Test")
                    .withOtoskopieLinks(List.of(OtoskopieResultat.SEROMUKOTYMPANON), null)
                    .withOtoskopieLinksResultatAndere("Test 2")
                    .withBemerkung("Eine weitere Otoskopie Bemerkung!")
            );

            UntersuchungGroesseGewicht untersuchungGroesseGewicht = UntersuchungGroesseGewichtBuilder.create(
                builder -> builder
                    .withBemerkung("Bemerkung")
                    .withBmi(BigDecimal.ZERO)
                    .withGewicht(BigDecimal.valueOf(122))
                    .withBmiPerzentile(PerzentileBmi.PERZENTILE_HOCH, null)
                    .withGewichtPerzentile(PerzentileGewicht.NORMAL, null)
                    .withGroesse(100)
                    .withGroessePerzentile(PerzentileGroesse.NICHT_GEPRUEFT, null)
            );

            UntersuchungArztBeurteilung untersuchungArztBrt = UntersuchungArztBeurteilungBuilder.create(
                builder -> builder
                    .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE))
                    .withBlutdruck(Blutdruck.AUFFAELLIG)
                    .withMundAndZaehne(List.of(MundAndZaehne.ADENOIDHYPERPLASIE, MundAndZaehne.KARIES))
                    .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                    .withFingeropposition(List.of(Fingeropposition.ALTERSENTSPRECHEND))
                    .withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT)
                    .withHaut(Haut.GROBKURSORISCH_UNAUFFAELLIG)
                    .withHerzauskultation(Herzauskultation.NICHT_GEPRUEFT)
                    .withVerhalten(Verhalten.AUFFAELLIG)
                    .withHampelmann(Hampelmann.MIND_FUENF_MAL)
                    .withBallFangen(BallFangen.BALL_GEFANGEN)
                    .withBallWerfen(BallWerfen.NICHT_GEPRUEFT)
                    .withSprachfaehigkeit(List.of(Sprachfaehigkeit.ARTIKULATIONSFEHLER, Sprachfaehigkeit.ANDERES))
            );

            UntersuchungArztBemerkung untersuchungArztBemerkung = UntersuchungArztBemerkungBuilder.create(
                builder -> builder.withBemerkungen("Eine ärztliche Bemerkung")
            );

            UntersuchungLaeusekontrolle untersuchungLaeusekontrolle = UntersuchungLaeusekontrolleBuilder.create(
                builder -> builder.withBeurteilung(List.of(LaeusekontrolleBeurteilung.LAEUSE_VORHANDEN))
                    .withBemerkung("Es gibt Läuse")
            );

            UntersuchungMassnahmen untersuchungMassnahmen = UntersuchungMassnahmenBuilder.create(
                builder -> builder.withBemerkung("Eine massnahmliche Bemerkung")
                    .withAbgegebenesInfomaterial(Set.of(InfomaterialBuilder.defaultInfomaterial()))
                    .withAnderes(true, "Eine andere massnahmliche Bemerkung")
                    .withNachkontrolle(true, false, false, true, false, false)
            );

            Untersuchung untersuchung =
                UntersuchungBuilder.create(builder ->
                    builder.withUntersuchungTriage(untersuchungTriage)
                        .withDurchgefuehrt(untersuchungDurchfuehrung)
                        .withUntersuchungSehen(untersuchungSehenBeurteilung)
                        .withUntersuchungHoerenAudiogramm(untersuchungHoerenAudiogramm)
                        .withUntersuchungHoerenOtoskopie(untersuchungHoerenOtoskopie)
                        .withUntersuchungGroesseGewicht(untersuchungGroesseGewicht)
                        .withUntersuchungArztBeurteilung(untersuchungArztBrt)
                        .withUntersuchungLaeusekontrolle(untersuchungLaeusekontrolle)
                        .withUntersuchungArztBemerkung(untersuchungArztBemerkung)
                        .withUntersuchungMassnahmen(untersuchungMassnahmen)
                );
            LocalDate anlassDatum = LocalDate.now();
            DateTimeRange dateRangeAnlass = DateTimeRange.of(anlassDatum.atTime(10, 0), anlassDatum.atTime(13, 30));
            Anlass anlassToday = AnlassBuilder.create(builder -> builder.withDatumSetSchuljahr(anlassDatum)
                .withZeitVonBis(dateRangeAnlass)
                .withArzt(BenutzerBuilder.create(bBuilder ->
                    bBuilder.withNachname("Univ.- Prof. em. Dr. Dr. h.c. mult.")
                        .withVorname("Francisco Domingo Carlos Andres Sebastián d'Anconia"))));
            UntersuchungImpfungIntern untersuchungImpfungIntern = UntersuchungImpfungInternBuilder.create(builder ->
                builder.withImpfstoff(ImpfstoffBuilder.defaultImpfstoff())
                    .withUserErfasst(BenutzerBuilder.defaultBenutzer())
                    .withUserDurchgefuehrt(BenutzerBuilder.defaultBenutzer())
                    .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                    .withDatumImpfung(LocalDate.now())
                    .withImmunitaetForKrankheit(Krankheit.MASERN, true)
                    .withImmunitaetForKrankheit(Krankheit.ROETELN, true)
                    .withImmunitaetForKrankheit(Krankheit.MUMPS, false));
            AnlassUntersuchungPrintDTO anlassTeilnahme = AnlassUntersuchungPrintDTO.ofEinladung(
                EinladungBuilder.create(builder ->
                    builder.withStatus(EinladungStatus.TEILNAHME_BESTAETIGT)
                        .withAnlass(anlassToday)
                        .withUntersuchung(untersuchung)),
                List.of(untersuchungImpfungIntern),
                List.of(UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder.withAnzahlImpfungen(10)
                    .withDatumLetzteImpfung(LocalDate.now()))),
                List.of(anlassWithDateTime));

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKind,
                List.of(anlassKeineTeilnahme, anlassTeilnahme),
                List.of(),
                LocalDate.now(),
                TenantEnum.BS,
                AppLanguages.DE,
                KinderdossierPrintVariant.ANLAESSE
            );

            KinderdossierPrinter.print(
                dto,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void anlass_with_data_everything_hidden(TenantEnum tenantEnum) {
        try (FileOutputStream outputStream = getFileOutputStream("Kinderdossier-anlaesse-all-hidden", tenantEnum)) {

            Kind kind = KindBuilder.defaultKind();
            List<KontaktPersonKind> kontaktPersonKind = List.of(
                KontaktPersonKindBuilder.defaultKontaktPersonKind(),
                KontaktPersonKindBuilder.defaultKontaktPersonKind());

            UntersuchungTriage untersuchungTriage =
                UntersuchungTriageBuilder.create(builder -> builder
                    .withTriageSu(true, "Das kommt schon gut :)"));
            UntersuchungDurchfuehrung untersuchungDurchfuehrung =
                UntersuchungDurchfuehrungBuilder.create(builder ->
                    builder.isNachzuegler(true)
                        .isNachkontrolle(true)
                        .isNeueintritt(false));

            UntersuchungSehenBeurteilung untersuchungSehenBeurteilung =
                UntersuchungSehenBeurteilungBuilder.create(builder -> builder.withBrilleLinsen(
                        BrilleLinsenResultat.MIT, null)
                    .withStereotest(StereotestResultat.NICHT_GEPRUEFT, null)
                    .withFarbsehen(FarbsehenResultat.PATHOLOGISCH, null)
                    .withVerwendeteTafel(VerwendetTafelResultat.BUCHSTABEN, null)
                    .withFernvisus(
                        List.of(DistanzSehenResultat.NICHT_GEPRUEFT, DistanzSehenResultat.RECHTS_REDUZIERT),
                        null)
                    .withFernvisusRechts(BigDecimal.valueOf(2.5))
                    .withNahvisus(List.of(DistanzSehenResultat.NICHT_GEPRUEFT), null)
                    .withMotilitaet(MotilitaetResultat.NORMAL, null)
                    .withStrabismus(List.of(StrabismusResultat.KEIN_STRABISMUS), null)
                    .withStrabismusAuspraegung(StrabismusAuspraegungResultat.LATENT, null)
                    .withStrabismusAuftreten(StrabismusAuftretenResultat.KEIN_STRABISMUS, null)
                    .withInspektion(InspektionKonvergenzResultat.AUFFAELLIG, null)
                    .withKonvergenz(InspektionKonvergenzResultat.UNAUFFAELLIG, null)
                    .withBemerkung("Eine sehr ausführliche bemerkung!")
                );

            UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm = UntersuchungHoerenAudiogrammBuilder.create(
                builder -> builder.withAudiogrammBeurteilung(
                        List.of(
                            AudiogrammBeurteilung.NICHT_GEPRUEFT,
                            AudiogrammBeurteilung.RECHTS_REDUZIERT), null)
                    .withAudiogrammRechts(Dezibel.DB50)
                    .withAudiogrammLinks(Dezibel.DB30)
                    .withZusaetzlicheAngaben(List.of(HoerenZusaetzlicheAngaben.ERKAELTUNG), null)
                    .withBemerkung("Eine weitere Audiogramm Bemerkung!")
            );

            UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie = UntersuchungHoerenOtoskopieBuilder.create(
                builder -> builder
                    .withOtoskopieRechts(
                        List.of(OtoskopieResultat.ANDERES, OtoskopieResultat.TROMMELFELL_CERUMEN),
                        null)
                    .withOtoskopieRechtsResultatAndere("Test")
                    .withOtoskopieLinks(List.of(OtoskopieResultat.SEROMUKOTYMPANON), null)
                    .withOtoskopieLinksResultatAndere("Test 2")
                    .withBemerkung("Eine weitere Otoskopie Bemerkung!")
            );

            UntersuchungGroesseGewicht untersuchungGroesseGewicht = UntersuchungGroesseGewichtBuilder.create(
                builder -> builder
                    .withBemerkung("Bemerkung")
                    .withBmi(BigDecimal.ZERO)
                    .withGewicht(BigDecimal.valueOf(122))
                    .withBmiPerzentile(PerzentileBmi.PERZENTILE_HOCH, null)
                    .withGewichtPerzentile(PerzentileGewicht.NORMAL, null)
                    .withGroesse(100)
                    .withGroessePerzentile(PerzentileGroesse.NICHT_GEPRUEFT, null)
            );

            UntersuchungArztBeurteilung untersuchungArztBrt = UntersuchungArztBeurteilungBuilder.create(
                builder -> builder
                    .withBewegungsapparat(List.of(Bewegungsapparat.SKOLIOSE))
                    .withBlutdruck(Blutdruck.AUFFAELLIG)
                    .withMundAndZaehne(List.of(MundAndZaehne.ADENOIDHYPERPLASIE, MundAndZaehne.KARIES))
                    .withEinbeinhuepfen(Einbeinhuepfen.SICHER)
                    .withFingeropposition(List.of(Fingeropposition.ALTERSENTSPRECHEND))
                    .withEinbeinstand(Einbeinstand.NICHT_GEPRUEFT)
                    .withHaut(Haut.GROBKURSORISCH_UNAUFFAELLIG)
                    .withHerzauskultation(Herzauskultation.NICHT_GEPRUEFT)
                    .withVerhalten(Verhalten.AUFFAELLIG)
                    .withSprachfaehigkeit(List.of(Sprachfaehigkeit.ARTIKULATIONSFEHLER, Sprachfaehigkeit.ANDERES))
            );

            UntersuchungArztBemerkung untersuchungArztBemerkung = UntersuchungArztBemerkungBuilder.create(
                builder -> builder.withBemerkungen("Mit einer Ärztlichen Bemerkung")
            );

            UntersuchungMassnahmen untersuchungMassnahmen = UntersuchungMassnahmenBuilder.create(
                builder -> builder.withBemerkung("Eine massnahmliche Bemerkung")
                    .withAbgegebenesInfomaterial(Set.of(InfomaterialBuilder.defaultInfomaterial()))
                    .withAnderes(true, "Eine andere massnahmliche Bemerkung")
                    .withNachkontrolle(true, false, false, true, false, false)
            );

            Untersuchung untersuchung =
                UntersuchungBuilder.create(builder ->
                    builder.withUntersuchungTriage(untersuchungTriage)
                        .withDurchgefuehrt(untersuchungDurchfuehrung)
                        .withUntersuchungSehen(untersuchungSehenBeurteilung)
                        .withUntersuchungHoerenAudiogramm(untersuchungHoerenAudiogramm)
                        .withUntersuchungHoerenOtoskopie(untersuchungHoerenOtoskopie)
                        .withUntersuchungGroesseGewicht(untersuchungGroesseGewicht)
                        .withUntersuchungArztBeurteilung(untersuchungArztBrt)
                        .withUntersuchungArztBemerkung(untersuchungArztBemerkung)
                        .withUntersuchungMassnahmen(untersuchungMassnahmen)
                );

            Anlass anlassToday = AnlassBuilder.create(builder -> builder.withDatumSetSchuljahr(LocalDate.now())
                .withArzt(BenutzerBuilder.defaultBenutzer())
                .withAnlassConfig(AnlassConfigBuilder.allSameRequirementSetting(RequirementSetting.NOT_VISIBLE)));
            AnlassUntersuchungPrintDTO anlassTeilnahme = AnlassUntersuchungPrintDTO.ofEinladung(
                EinladungBuilder.create(builder ->
                    builder.withStatus(EinladungStatus.TEILNAHME_BESTAETIGT)
                        .withAnlass(anlassToday)
                        .withUntersuchung(untersuchung)),
                List.of(),
                List.of(),
                List.of());

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKind,
                List.of(anlassTeilnahme),
                List.of(),
                LocalDate.now(),
                TenantEnum.BS,
                AppLanguages.DE,
                KinderdossierPrintVariant.ANLAESSE
            );

            KinderdossierPrinter.print(
                dto,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anonymer_eintrag_rendersHash_andSkipsPersonalData(TenantEnum tenantEnum) throws IOException {
        // ARRANGE: an anonymous entry has no Kind/Dossier/Einladung. We build the Einladung-less
        // print DTO exactly like AnonymerEintragPrintService does and render it through the
        // dedicated AnonymerEintragPrinter.
        String hash = "abc123-anonymous-hash";
        String schulhausName = "Geheime Primarschule Musterberg";

        // A name that must never leak into the anonymous document.
        Kind kind = KindBuilder.create(builder -> builder.withName("Streng-Geheim", "Persönlich"));
        Schule schule = SchuleBuilder.create(builder -> builder.withSchulhaus(schulhausName));
        Klasse klasse = KlasseBuilder.create(builder -> builder
            .forSchule(schule)
            .withBezeichnung("4a")
            .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_4)
            .withKlasseSchulart(KlasseBuilder.KLASSENTYP_PRIM));
        kind.setKlasse(klasse);

        UntersuchungTriage untersuchungTriage =
            UntersuchungTriageBuilder.create(builder -> builder.withTriageSu(true, "Alles gut"));
        Untersuchung untersuchung =
            UntersuchungBuilder.create(builder -> builder.withUntersuchungTriage(untersuchungTriage));
        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(LocalDate.now())
            .withAnlassTyp(AnlassTyp.KLASSE_4_ANONYM));

        AnlassUntersuchungPrintDTO anlassDTO = AnlassUntersuchungPrintDTO.anonymous(
            anlass,
            untersuchung,
            hash,
            List.of(),
            List.of(),
            List.of());

        AnonymerEintragPrintDTO dto = new AnonymerEintragPrintDTO(
            hash,
            List.of(anlassDTO),
            LocalDate.now(),
            tenantEnum,
            AppLanguages.DE);

        // ACT: render into memory (for assertions) and to a file (for visual inspection like the
        // other printer tests).
        byte[] pdfBytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            AnonymerEintragPrinter.print(dto, byteArrayOutputStream, fileStoreServiceMock, true);
            pdfBytes = byteArrayOutputStream.toByteArray();
        }
        try (FileOutputStream outputStream = getFileOutputStream("anonymer-eintrag", tenantEnum)) {
            outputStream.write(pdfBytes);
        }

        // ASSERT
        assertThat(pdfBytes).isNotEmpty();

        String pdfText = extractPdfText(pdfBytes);
        // The hash is rendered in the title, footer and under the Anlasstitel.
        assertThat(pdfText)
            .contains(hash)
            .contains("anonym")
            .doesNotContain(kind.getName())
            .doesNotContain(kind.getVorname())
            .doesNotContain(schulhausName)
            .doesNotContain("4a");
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anonymer_eintrag_withoutUntersuchung_stillRendersHash(TenantEnum tenantEnum) throws IOException {
        // ARRANGE: an anonymous entry may not have an Untersuchung yet (untersuchung == null).
        String hash = "no-untersuchung-hash";
        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(LocalDate.now())
            .withAnlassTyp(AnlassTyp.KLASSE_4_ANONYM));

        AnlassUntersuchungPrintDTO anlassDTO = AnlassUntersuchungPrintDTO.anonymous(
            anlass,
            null,
            hash,
            List.of(),
            List.of(),
            List.of());

        AnonymerEintragPrintDTO dto = new AnonymerEintragPrintDTO(
            hash,
            List.of(anlassDTO),
            LocalDate.now(),
            tenantEnum,
            AppLanguages.DE);

        // ACT
        byte[] pdfBytes;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            AnonymerEintragPrinter.print(dto, byteArrayOutputStream, fileStoreServiceMock, true);
            pdfBytes = byteArrayOutputStream.toByteArray();
        }

        // ASSERT
        assertThat(pdfBytes).isNotEmpty();
        assertThat(extractPdfText(pdfBytes)).contains(hash);
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

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void notizen_kinderdossier_with_files(TenantEnum tenantEnum) {
        try (InputStream pdfInput = BasePrinter.class.getResourceAsStream("/FileUtilTest/pdfFileExample.pdf");
            InputStream pictureInput = BasePrinter.class.getResourceAsStream("/FileUtilTest/portrait.jpg");
            FileOutputStream outputStream = getFileOutputStream("Kinderdossier-notizen", tenantEnum)) {

            if (pdfInput != null && pictureInput != null) {
                Mockito.when(fileStoreServiceMock.getFileBytesSync("picture")).thenReturn(pictureInput.readAllBytes());
                Mockito.when(fileStoreServiceMock.getFileBytesSync("pdf")).thenReturn(pdfInput.readAllBytes());
            }

            Kind kind = KindBuilder.defaultKind();
            List<KontaktPersonKind> kontaktPersonKind = List.of(
                KontaktPersonKindBuilder.defaultKontaktPersonKind(),
                KontaktPersonKindBuilder.defaultKontaktPersonKind());

            List<FileData> files = List.of(
                FileDataBuilder.defaultImgFileData(),
                FileDataBuilder.create(builder -> builder.withMimeType(MediaType.parse(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))),
                FileDataBuilder.defaultPdfFileData()
            );

            List<Notiz> notizes = List.of(
                NotizBuilder.create(builder -> builder.withFiles(files)
                    .withBemerkungen(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur blandit ante. "
                            + "Pellentesque a urna felis. Donec finibus tempor augue, at tempor orci cursus quis. "
                            + "Nam ultricies risus neque, et laoreet lectus vulputate sed. Sed facilisis placerat "
                            + "semper. Proin justo libero, facilisis eu augue non, fermentum tempor tortor. Nunc "
                            + "luctus luctus tristique. Now a newline \nNextline. Now a tab \t after tab")),
                NotizBuilder.create(builder -> builder.withFiles(files)
                    .withTimestampErstellt(OffsetDateTime.now().minusDays(4))
                    .withBemerkungen(
                        "Lange ist es heeeeer!"))
            );

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                kontaktPersonKind,
                List.of(),
                notizes,
                LocalDate.now(),
                TenantEnum.BS,
                AppLanguages.DE,
                KinderdossierPrintVariant.NOTIZEN
            );

            KinderdossierPrinter.print(
                dto,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void kinderdossier_minimal_data(TenantEnum tenantEnum) {
        try (FileOutputStream outputStream = getFileOutputStream("kinderdossier-minimal", tenantEnum)) {
            Kind kind = KindBuilder.defaultKindWithNullValues();

            KinderdossierPrintDTO dto = new KinderdossierPrintDTO(
                kind,
                List.of(),
                List.of(),
                List.of(),
                LocalDate.now(),
                TenantEnum.BS,
                AppLanguages.DE,
                KinderdossierPrintVariant.GESAMTES
            );

            KinderdossierPrinter.print(
                dto,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anlaesse_printer_minimal_date(TenantEnum tenantEnum) throws IOException {
        // ARRANGE
        try (FileOutputStream outputStream = getFileOutputStream("anlass-print", tenantEnum)) {
            Anlass anlass = AnlassBuilder.defaultAnlass(LocalDate.of(2020, 1, 1));
            Einladung einladung = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass));

            AnlassPlanungPrintDTO anlassPrintDTO =
                new AnlassPlanungPrintDTO(anlass, List.of(einladung), LocalDate.now(), tenantEnum, AppLanguages.DE, null);

            AnlassPlanungPrinter.print(
                anlassPrintDTO,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);

        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
            throw e;
        }

    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anlaesse_printer_anlass_without_titel(TenantEnum tenantEnum) throws IOException {
        // ARRANGE: Anlass without titel — verifies no NPE and no " null" artifacts in title/footer
        try (FileOutputStream outputStream = getFileOutputStream("anlass-print-no-titel", tenantEnum)) {
            Anlass anlass = AnlassBuilder.create(b -> b
                .withDatumSetSchuljahr(LocalDate.of(2020, 1, 1))
                .withTitle(null));
            Einladung einladung = EinladungBuilder.create(builder -> builder.withAnlass(anlass));

            AnlassPlanungPrintDTO anlassPrintDTO =
                new AnlassPlanungPrintDTO(anlass, List.of(einladung), LocalDate.now(), tenantEnum, AppLanguages.DE, null);

            AnlassPlanungPrinter.print(
                anlassPrintDTO,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
            throw e;
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void merge_pdfWithInputFields(TenantEnum tenantEnum) {
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/FileUtilTest/pdfWithInputFields.pdf");
            FileOutputStream outputStream = getFileOutputStream("input-merge", tenantEnum)
        ) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, tenantEnum, fileStoreServiceMock);
            Document document = basePrinter.getDocument(outputStream);

            basePrinter.addHeader(document, true);

            FileData fileData = FileDataBuilder.defaultPdfFileData();

            if (inputStream != null) {
                Mockito.when(fileStoreServiceMock.getFileBytesSync(any())).thenReturn(inputStream.readAllBytes());
                basePrinter.addFile(fileData, document);
            }

            document.close();
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void merge_plainPdf(TenantEnum tenantEnum) {
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/FileUtilTest/pdfFileExample.pdf");
            FileOutputStream outputStream = getFileOutputStream("plain-merge", tenantEnum)
        ) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, tenantEnum, fileStoreServiceMock);
            Document document = basePrinter.getDocument(outputStream);

            basePrinter.addHeader(document, true);

            FileData fileData = FileDataBuilder.defaultPdfFileData();

            if (inputStream != null) {
                Mockito.when(fileStoreServiceMock.getFileBytesSync(any())).thenReturn(inputStream.readAllBytes());
                basePrinter.addFile(fileData, document);
            }

            document.close();
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void merge_image(TenantEnum tenantEnum) {
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream("/FileUtilTest/portrait.jpg");
            FileOutputStream outputStream = getFileOutputStream("image-merge", tenantEnum)
        ) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, tenantEnum, fileStoreServiceMock);
            Document document = basePrinter.getDocument(outputStream);

            basePrinter.addHeader(document, true);

            FileData fileData = FileDataBuilder.defaultImgFileData();

            if (inputStream != null) {
                Mockito.when(fileStoreServiceMock.getFileBytesSync(any())).thenReturn(inputStream.readAllBytes());
                basePrinter.addFile(fileData, document);
            }

            document.close();
        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
        }
    }

    private FileOutputStream getFileOutputStream(@NonNull String filename, @NonNull TenantEnum tenant)
        throws FileNotFoundException {
        return new FileOutputStream(getTempFilenamePath(filename, tenant));
    }

    private static String getTempFilenamePath(@NonNull String filename, @NonNull TenantEnum tenant) {
        String path = OUTPUT_PATH + '/' + tenant.name() + '/' + filename + ".pdf";
        LOG.info("Writing to {}", path);
        return path;
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anlaesse_printer_complete_data_multiple_children(TenantEnum tenantEnum) throws IOException {
        // ARRANGE
        try (FileOutputStream outputStream = getFileOutputStream("anlass-print-complete", tenantEnum)) {
            LocalDate anlassDatum = LocalDate.of(2020, 2, 13);
            Anlass anlass = AnlassBuilder.defaultAnlass(anlassDatum);
            anlass.setZeitVonBis(DateTimeRange.of(anlassDatum.atTime(14, 0), anlassDatum.atTime(16, 0)));
            anlass.setArzt(BenutzerBuilder.create(bBuilder -> bBuilder.withVorname("Dominique")
                .withNachname("Doktorinavitch")));
            anlass.setMpa(BenutzerBuilder.defaultBenutzer());
            anlass.setWeiterePerson(BenutzerBuilder.defaultBenutzer());

            anlass.setArztInfo("Dies wird eine lange Untersuchung weil sie lang ist und nicht kurz da sie eben lang "
                + "ist");
            anlass.setMpaInfo("Es gibt in dieser Klasse viele Kinder wir müssen prüfen ob diese Kinder alle innerhalb "
                + "der Erwartungen liegen. Ausserm werden wir bei Kindern die nicht in der Norm zu sein scheinen "
                + "potentiell Massnahmen zur durchführung vorschlagen");

            // Create different Schulen
            Schule schule1 = SchuleBuilder.create(builder -> builder
                .withSchulhaus("Primarschule Musterberg")
                .withSchulstandort("Musterberg Zentrum")
                .withSchulhausnummer("001"));

            Schule schule2 = SchuleBuilder.create(builder -> builder
                .withSchulhaus("Sekundarschule Bergtal")
                .withSchulstandort("Bergtal Nordquartier Hauptstandort")
                .withSchulhausnummer("002"));

            Schule schule3 = SchuleBuilder.create(builder -> builder
                .withSchulhaus("Gymnasium Stadtmitte")
                .withSchulstandort("Stadtmittenhof")
                .withSchulhausnummer("003"));

            // Create different Klassen for each Schule
            Klasse klasse1a = KlasseBuilder.create(builder -> builder
                .forSchule(schule1)
                .withBezeichnung("1a")
                .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_1)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_PRIM));

            Klasse klasse4b = KlasseBuilder.create(builder -> builder
                .forSchule(schule1)
                .withBezeichnung("4b")
                .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_4)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_PRIM));

            Klasse klasse7c = KlasseBuilder.create(builder -> builder
                .forSchule(schule2)
                .withBezeichnung("7c")
                .withKlassenstufe(Klassenstufe.SEK_I_KLASSE_1)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_SEKE));

            Klasse klasseKG = KlasseBuilder.create(builder -> builder
                .forSchule(schule3)
                .withBezeichnung("KG-Rot")
                .withKlassenstufe(Klassenstufe.KINDERGARTEN_1)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_KG));

            // Create Einladungen for different Kinder with different Schulen and Klassen
            Einladung einladung1 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasse1a)
                        .withName("Mustermann", "Annemaria"))))));

            Einladung einladung2 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasse4b)
                        .withEsadId(EdokidIdTestGenerator.getNextEdokidId("001").toFormattedString())
                        .withName("Schmidt", "Benjamin"))))));

            Einladung einladung3 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasse7c)
                        .withName("Weber", "Clara")))))
                .withNotiz(NotizBuilder.create(
                    notizBuilder -> notizBuilder.withNotizTyp(NotizTyp.IMPFAUSWEIS)
                        .withFiles(List.of(FileDataBuilder.defaultPdfFileData()))
                ))
                .withNotiz(NotizBuilder.create(
                    notizBuilder -> notizBuilder.withNotizTyp(NotizTyp.FRAGEBOGEN)
                        .withFiles(List.of(FileDataBuilder.defaultPdfFileData()))
                ))
            );

            Notiz notizImpfausweis = NotizBuilder.defaultNotiz();
            notizImpfausweis.setTyp(NotizTyp.IMPFAUSWEIS);
            Notiz notizFragebogen = NotizBuilder.defaultNotiz();
            notizFragebogen.setTyp(NotizTyp.FRAGEBOGEN);

            Einladung einladung4 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasseKG)
                        .withGeschlecht(Geschlecht.DIVERS)
                        .withName("Fischer", "David Mario")))))
                .withNotiz(notizImpfausweis)
                .withNotiz(notizFragebogen)
            );
            var ud = UntersuchungDurchfuehrungBuilder.create(builder -> builder.isNachkontrolle(true));
            var u = UntersuchungBuilder.create(builder -> builder.withDurchgefuehrt(ud));
            einladung4.setUntersuchung(u);

            Schule schule4 = SchuleBuilder.create(builder -> builder
                .withSchulhaus("Kindergarten Sonnenblume")
                .withSchulstandort("Sonnenblumengasse")
                .withSchulhausnummer("004"));

            Klasse klasseKG2 = KlasseBuilder.create(builder -> builder
                .forSchule(schule4)
                .withBezeichnung("KG-Blau")
                .withKlassenstufe(Klassenstufe.KINDERGARTEN_2)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_KG));

            Einladung einladung5 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withStatus(EinladungStatus.TEILNAHME_ABGESAGT)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasseKG2)
                        .withEsadId("A0 11 111 11")
                        .withName("Vonwildebergenstein-Huguenin", "Bartholomäus-Maximilianus"))))));

            List<Einladung> einladungen = List.of(einladung1, einladung2, einladung3, einladung4, einladung5);

            AnlassPlanungPrintDTO anlassPrintDTO =
                new AnlassPlanungPrintDTO(anlass, einladungen, LocalDate.now(), tenantEnum, AppLanguages.DE, null);

            AnlassPlanungPrinter.print(
                anlassPrintDTO,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);

        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
            throw e;
        }
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = "DEFAULT", mode = Mode.EXCLUDE)
    void anlass_withouth_durchfuehrung_but_nachkontrolle_on_dossier(TenantEnum tenantEnum) throws IOException {
        // ARRANGE
        try (FileOutputStream outputStream = getFileOutputStream("anlass-print-complete", tenantEnum)) {
            LocalDate anlassDatum = LocalDate.of(2020, 2, 13);
            Anlass anlass = AnlassBuilder.defaultAnlass(anlassDatum);

            // Create different Schulen
            Schule schule1 = SchuleBuilder.create(builder -> builder
                .withSchulhaus("Primarschule Musterberg")
                .withSchulstandort("Musterberg Zentrum")
                .withSchulhausnummer("001"));

            // Create different Klassen for each Schule
            Klasse klasse1a = KlasseBuilder.create(builder -> builder
                .forSchule(schule1)
                .withBezeichnung("1a")
                .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_1)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_PRIM));

            // Create Einladungen for different Kinder with different Schulen and Klassen
            Einladung einladung1 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasse1a)
                        .withName("Mustermann", "Annemaria"))))));
            Nachkontrolle nachkontrolle1 =
                NachkontrolleBuilder.create(builder -> builder.forDossier(einladung1.getDossier())
                    .withNachkontrolleHoeren(true));

            Einladung einladung2 = EinladungBuilder.create(builder -> builder
                .withAnlass(anlass)
                .withDossier(DossierBuilder.create(dossierBuilder -> dossierBuilder
                    .forKind(KindBuilder.create(kindBuilder -> kindBuilder
                        .withKlasse(klasse1a)
                        .withEsadId(EdokidIdTestGenerator.getNextEdokidId("001").toFormattedString())
                        .withName("Schmidt", "Benjamin"))))));
            Nachkontrolle nachkontrolle2 =
                NachkontrolleBuilder.create(builder -> builder.forDossier(einladung2.getDossier())
                    .withNachkontrolleHoeren(false));

            List<Einladung> einladungen = List.of(einladung1, einladung2);
            List<Nachkontrolle> nachkontrollen = List.of(nachkontrolle1, nachkontrolle2);

            AnlassPlanungPrintDTO anlassPrintDTO =
                new AnlassPlanungPrintDTO(anlass, einladungen, LocalDate.now(), tenantEnum, AppLanguages.DE, nachkontrollen);

            AnlassPlanungPrinter.print(
                anlassPrintDTO,
                outputStream,
                fileStoreServiceMock,
                tenantSettingsServiceMock,
                configServiceMock);

        } catch (IOException e) {
            LOG.error("Error while writing to file", e);
            throw e;
        }
    }
}
