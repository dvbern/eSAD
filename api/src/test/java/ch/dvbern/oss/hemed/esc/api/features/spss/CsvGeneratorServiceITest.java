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

package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.oss.hemed.esc.api.testutil.TestUtil.loadTestfile;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class CsvGeneratorServiceITest extends AbstractITest {

    private final CsvGeneratorService csvGeneratorService;
    private final FixtureService fixtureService;
    private final AnlassService anlassService;
    private final EinladungService einladungService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final BenutzerService benutzerService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @ParameterizedTest
    @MethodSource("spssReportResults")
    @TestAsAdminBS
    void generateSpssReport(
        @NonNull SpssReportType spssReportType,
        @NonNull String expectedCsvPath,
        @NonNull CleanFileName fileName
    ) {
        // ARRANCE
        LocalDate anlassDatum = LocalDate.of(2025, 6, 22);
        AnlassConfig anlassConfig = fixtureService.createAnlassConfigAllOptional(
            anlassDatum.getYear(),
            spssReportType.getAnlassStatusList().getFirst());
        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(anlassDatum)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass.setAnlassConfig(anlassConfig);
        anlassService.persist(anlass);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        Path result =
            csvGeneratorService.generateSpssReport(
                spssReportType,
                EmailAddress.fromString("admin@example.com"),
                fileName);
        // ASSERT
        // Before it was not exported
        assertThat(einladung.isSpssExported()).isFalse();

        // Now its exported
        Einladung einladungAfter = einladungService.getById(einladung.getId());
        assertThat(einladungAfter.isSpssExported()).isTrue();

        // And the result contains our expectations
        File expectedCsv = loadTestfile(expectedCsvPath);
        List<String> expectedStrings =
            FileUtil.readLineBasedTextFromPath(expectedCsv.toPath()); // read excted csv lines
        List<String> actualStrings = FileUtil.readLineBasedTextFromPath(result);

        // all expected lines must be present
        assertThat(expectedStrings).isNotEmpty()
            .allSatisfy(expectedString -> assertThat(actualStrings).as(
                    "actual should contain expected string: %s",
                    expectedString)
                .contains(expectedString));
    }

    @ParameterizedTest
    @MethodSource("csvReportResults")
    @TestAsAdminBS
    void generateCsvReport(
        @NonNull CsvReportType csvReportType,
        @NonNull String expectedCsvPath,
        @NonNull CleanFileName fileName
    ) {
        // ARRANCE
        LocalDate anlassDatum = LocalDate.of(2025, 8, 22);
        AnlassConfig anlassConfig = fixtureService.createAnlassConfigAllOptional(
            anlassDatum.getYear(),
            AnlassTyp.KINDERGARTEN);

        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(anlassDatum)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass.setAnlassConfig(anlassConfig);
        anlassService.persist(anlass);

        // Für diesen Report werden sämtliche Anlässe exportiert auch wenn der Status noch nicht abgeschlossen ist.
        Anlass anlass2 = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(anlassDatum)
            .withAnlassStatus(AnlassStatus.IN_PLANUNG));
        anlass2.setAnlassConfig(anlassConfig);
        anlassService.persist(anlass2);

        // SystemId wird im Report ausgegeben und daher muss diese fix sein.
        String fixedUUID = "ddf7d7bf-e3ba-45b8-a89c-501e5e5d6059";
        UUID uuid = UUID.nameUUIDFromBytes(fixedUUID.getBytes(StandardCharsets.UTF_8));
        Kind kind = fixtureService.createKind(ID.of(uuid, Kind.class), "Barbara", "Testname", Geschlecht.WEIBLICH);
        Dossier dossier = fixtureService.createDossier(kind);
        fixtureService.createEinladung(anlass, dossier);

        // Für diesen Report werden sämtliche Einladungen exportiert auch wenn die Teilnahme Abgesagt ist.
        fixtureService.createEinladung(anlass2, dossier, EinladungStatus.TEILNAHME_ABGESAGT);

        DateRange zeitraum = new DateRange();
        zeitraum.setDatumVon(LocalDate.of(2025, 8, 1));
        zeitraum.setDatumBis(LocalDate.of(2025, 8, 31));

        // ACT
        Path result =
            csvGeneratorService.generateCsvReport(
                csvReportType,
                zeitraum,
                EmailAddress.fromString("admin@example.com"), fileName);

        // And the result contains our expectations
        File expectedCsv = loadTestfile(expectedCsvPath);
        List<String> expectedStrings =
            FileUtil.readLineBasedTextFromPath(expectedCsv.toPath()); // read excted csv lines
        List<String> actualStrings = FileUtil.readLineBasedTextFromPath(result);

        // all expected lines must be present
        assertThat(expectedStrings).isNotEmpty()
            .allSatisfy(expectedString -> assertThat(actualStrings).as(
                    "actual should contain expected string: %s",
                    expectedString)
                .contains(expectedString));
    }

    @ParameterizedTest
    @MethodSource("csvImpfstoffReportResults")
    @TestAsAdminBS
    void generateCsvImpfstoffReport(
        @NonNull CsvReportType csvReportType,
        @NonNull String expectedCsvPath,
        @NonNull CleanFileName fileName
    ) {
        /**
         * Folgende Fälle sollten abgedeckt sein:
         *  - Kind in einem Anlass mit Impfungen; (Impfungen ausgeben);
         *  - Kind ist in 2 Anlässen mit Impfungen; Im alten Anlass wird 1 Impfung erfasst und im neuen wird 1 zusätztliche Impfung erfasst.
         *      (Es werden 2 Impfungen ausgegeben, die Impfung vom alten Anlass wird automatisch in den neuen Anlass kopiert und die neu erfasste)
         *  - Kind ist in 2 Anlässen aber der neuere hat keine Untersuchung (Impfungen von älteren Anlass sollten
         *  ausgegeben werden);
         */

        // ARRANGE
        LocalDate anlassDatum1 = LocalDate.of(2026, 8, 1);
        AnlassConfig anlassConfig1 = fixtureService.createAnlassConfigAllOptional(
            anlassDatum1.getYear(),
            AnlassTyp.KINDERGARTEN);
        Anlass anlass1 = AnlassBuilder.create(builder -> builder
            .withAnlassTyp(AnlassTyp.KINDERGARTEN)
            .withDatumSetSchuljahr(anlassDatum1)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass1.setAnlassConfig(anlassConfig1);
        anlassService.persist(anlass1);

        LocalDate anlassDatum2 = LocalDate.of(2026, 8, 10);
        AnlassConfig anlassConfig2 = fixtureService.createAnlassConfigAllOptional(
            anlassDatum2.getYear(),
            AnlassTyp.KLASSE_4);
        Anlass anlass2 = AnlassBuilder.create(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_4)
            .withDatumSetSchuljahr(anlassDatum2)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass2.setAnlassConfig(anlassConfig2);
        anlassService.persist(anlass2);

        AnlassConfig anlass2OhneImpflisteConfig = fixtureService.createAnlassConfigAllNotVisible(
            anlassDatum2.getYear(),
            AnlassTyp.EINSCHULUNG);
        Anlass anlass2OhneImpfliste = AnlassBuilder.create(builder -> builder
            .withAnlassTyp(AnlassTyp.EINSCHULUNG)
            .withDatumSetSchuljahr(anlassDatum2)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass2OhneImpfliste.setAnlassConfig(anlass2OhneImpflisteConfig);
        anlassService.persist(anlass2OhneImpfliste);

        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withNachname("Meier").withVorname("Maria"));
        benutzerService.persist(benutzer);
        Impfstoff impfstoff = ImpfstoffBuilder.create(builder -> builder.withName("Priorix_Test"));
        impfstoffService.persist(impfstoff);
        ImpfstoffCharge charge =
            ImpfstoffChargeBuilder.create(builder -> builder.forImpfstoff(impfstoff).withName("Priorix_Charge_Test"));
        impfstoffChargeService.persist(charge);

        Impfstoff impfstoff2 = ImpfstoffBuilder.create(builder -> builder.withName("Masern_Test"));
        impfstoffService.persist(impfstoff2);
        ImpfstoffCharge charge2 =
            ImpfstoffChargeBuilder.create(builder -> builder.forImpfstoff(impfstoff2).withName("Masern_Charge_Test"));
        impfstoffChargeService.persist(charge2);

        Impfstoff impfstoff3 = ImpfstoffBuilder.create(builder -> builder.withName("FSME_Test"));
        impfstoffService.persist(impfstoff3);
        ImpfstoffCharge charge3 =
            ImpfstoffChargeBuilder.create(builder -> builder.forImpfstoff(impfstoff3).withName("FSME_Charge_Test"));
        impfstoffChargeService.persist(charge3);

        // Fall 1 - Kind in einem Anlass mit Impfungen; (Impfungen ausgeben);

        // SystemId wird im Report ausgegeben und daher muss diese fix sein.
        String fixedUUID = "58ad78f8-88da-40ad-8e6a-2e6786e3a54e";
        UUID uuid = UUID.nameUUIDFromBytes(fixedUUID.getBytes(StandardCharsets.UTF_8));
        Kind kind = fixtureService.createKind(ID.of(uuid, Kind.class), "Barbara", "Testname", Geschlecht.WEIBLICH);
        Dossier dossier = fixtureService.createDossier(kind);
        Einladung einladung = fixtureService.createEinladung(anlass1, dossier);

        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        UntersuchungImpfungIntern impfungen = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen);

        // End Fall 1

        // Fall 2 - Kind ist in 2 Anlässen mit Impfungen; (Impfungen vom neueren Anlass sollten ausgegeben werden)

        String fixedUUID_fall_2 = "d99f3fe6-5f86-4086-ac0b-7b70f81b0ec6";
        UUID uuid_fall_2 = UUID.nameUUIDFromBytes(fixedUUID_fall_2.getBytes(StandardCharsets.UTF_8));
        Kind kind_fall2 =
            fixtureService.createKind(ID.of(uuid_fall_2, Kind.class), "John", "Smith", Geschlecht.MAENNLICH);
        Dossier dossier_fall_2 = fixtureService.createDossier(kind_fall2);
        Einladung einladung_fall_2_anlass_1 = fixtureService.createEinladung(anlass1, dossier_fall_2);

        Untersuchung untersuchung_fall_2_untersuchung_1 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_2_anlass_1);
        assertThat(untersuchung_fall_2_untersuchung_1.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungIntern impfungen_fall_2_impfung_1 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_2_untersuchung_1.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_2_impfung_1);

        Einladung einladung_fall_2_anlass_2 = fixtureService.createEinladung(anlass2, dossier_fall_2);
        Untersuchung untersuchung_fall_2_untersuchung_2 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_2_anlass_2);
        assertThat(untersuchung_fall_2_untersuchung_2.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungIntern impfungen_fall_2_impfung_2 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_2_untersuchung_2.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff2)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge2)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_2_impfung_2);

        // End Fall 2

        // Fall 3 - Kind ist in 2 Anlässen aber der neuere hat keine Impfungen (Impfungen von älteren Anlass sollten ausgegeben werden);

        String fixedUUID_fall_3 = "716d378c-915e-4b9d-be63-a2ef34bddfeb";
        UUID uuid_fall_3 = UUID.nameUUIDFromBytes(fixedUUID_fall_3.getBytes(StandardCharsets.UTF_8));
        Kind kind_fall3 =
            fixtureService.createKind(ID.of(uuid_fall_3, Kind.class), "Frodo", "Beutlin", Geschlecht.UNBEKANNT);
        Dossier dossier_fall_3 = fixtureService.createDossier(kind_fall3);
        Einladung einladung_fall_3_anlass_1 = fixtureService.createEinladung(anlass1, dossier_fall_3);

        Untersuchung untersuchung_fall_3_untersuchung_1 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_3_anlass_1);
        assertThat(untersuchung_fall_3_untersuchung_1.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungIntern impfungen_fall_3_impfung_1 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_3_untersuchung_1.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff2)
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withCharge(charge)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_3_impfung_1);

        Einladung einladung_fall_3_anlass_2 = fixtureService.createEinladung(anlass2, dossier_fall_3);
        assertThat(einladung_fall_3_anlass_2.getUntersuchung()).isNull();

        // End Fall 3

        // Fall 4 - Kind mit 4 Impfungen: 2 GEPLANT (gleicher Impfstoff) + 2 DURCHGEFUEHRT (gleicher Impfstoff)
        // Erwartet: 3 Impfungen im Report (1 GEPLANT dedupliziert + 2 DURCHGEFUEHRT alle)

        String fixedUUID_fall_4 = "f2472fa6-edee-44db-872f-a6edeec4db67";
        UUID uuid_fall_4 = UUID.nameUUIDFromBytes(fixedUUID_fall_4.getBytes(StandardCharsets.UTF_8));
        Kind kind_fall4 =
            fixtureService.createKind(
                ID.of(uuid_fall_4, Kind.class),
                "Sam",
                "Gamdschie",
                LocalDate.of(2000, 12, 12),
                Geschlecht.MAENNLICH);
        Dossier dossier_fall_4 = fixtureService.createDossier(kind_fall4);
        Einladung einladung_fall_4_anlass_1 = fixtureService.createEinladung(anlass1, dossier_fall_4);

        Untersuchung untersuchung_fall_4_untersuchung_1 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_4_anlass_1);
        assertThat(untersuchung_fall_4_untersuchung_1.getUntersuchungImpfungen()).isNotNull();

        // 2 GEPLANT for the same impfstoff (only youngest should be kept)
        UntersuchungImpfungIntern impfungen_fall_4_geplant_1 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_4_untersuchung_1.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withDatumImpfung(null)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_4_geplant_1);

        UntersuchungImpfungIntern impfungen_fall_4_geplant_2 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_4_untersuchung_1.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withDatumImpfung(LocalDate.of(2026, 8, 20))
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_4_geplant_2);

        // 2 DURCHGEFUEHRT for the same impfstoff (both should be kept)
        Einladung einladung_fall_4_anlass_2 = fixtureService.createEinladung(anlass2, dossier_fall_4);
        Untersuchung untersuchung_fall_4_untersuchung_2 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_4_anlass_2);
        assertThat(untersuchung_fall_4_untersuchung_2.getUntersuchungImpfungen()).isNotNull();

        UntersuchungImpfungIntern impfungen_fall_4_durchgefuehrt_1 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_4_untersuchung_2.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff3)
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withCharge(charge3)
                .withDatumImpfung(LocalDate.of(2024, 7, 10))
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_4_durchgefuehrt_1);

        UntersuchungImpfungIntern impfungen_fall_4_durchgefuehrt_2 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_4_untersuchung_2.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff3)
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withCharge(charge3)
                .withDatumImpfung(LocalDate.of(2025, 7, 25))
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_4_durchgefuehrt_2);

        // End Fall 4

        // Fall 5 - Kind ist in 2 Anlässen. Im älteren Fall gibt es eine Impfung, die in den neueren Fall übernommen wurde
        // Dort ist aber die Tabelle Interne-Impfungen gemäss AnlassConfig deaktiviert -> es soll der ältere Fall übernommen werden.

        String fixedUUID_fall_5 = "c1344cb6-24b6-454d-8137-58efdff9d680";
        UUID uuid_fall_5 = UUID.nameUUIDFromBytes(fixedUUID_fall_5.getBytes(StandardCharsets.UTF_8));
        Kind kind_fall5 =
            fixtureService.createKind(ID.of(uuid_fall_5, Kind.class), "Hanna", "Meier", Geschlecht.WEIBLICH);
        Dossier dossier_fall_5 = fixtureService.createDossier(kind_fall5);

        Einladung einladung_fall_5_anlass_1 = fixtureService.createEinladung(anlass1, dossier_fall_5);
        Untersuchung untersuchung_fall_5_untersuchung_1 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_5_anlass_1);
        assertThat(untersuchung_fall_5_untersuchung_1.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungIntern impfungen_fall_5_impfung_1 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_5_untersuchung_1.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_5_impfung_1);

        Einladung einladung_fall_5_anlass_2 = fixtureService.createEinladung(anlass2OhneImpfliste, dossier_fall_5);
        Untersuchung untersuchung_fall_5_untersuchung_2 =
            fixtureService.createUntersuchungWithImpfungen(einladung_fall_5_anlass_2);
        assertThat(untersuchung_fall_5_untersuchung_2.getUntersuchungImpfungen()).isNotNull();
        UntersuchungImpfungIntern impfungen_fall_5_impfung_2 = UntersuchungImpfungInternBuilder.create(builder ->
            builder.forUntersuchungImpfungen(untersuchung_fall_5_untersuchung_2.getUntersuchungImpfungen())
                .withImpfstoff(impfstoff)
                .withImpfStatus(ImpfStatus.GEPLANT)
                .withCharge(charge)
                .withUserErfasst(benutzer));
        untersuchungImpfungInternService.persist(impfungen_fall_5_impfung_2);

        // End Fall 5

        DateRange zeitraum = new DateRange();
        zeitraum.setDatumVon(LocalDate.of(2026, 7, 31));
        zeitraum.setDatumBis(LocalDate.of(2026, 8, 31));

        // ACT
        Path result =
            csvGeneratorService.generateCsvReport(
                csvReportType,
                zeitraum,
                EmailAddress.fromString("admin@example.com"),
                fileName);

        // And the result contains our expectations
        File expectedCsv = loadTestfile(expectedCsvPath);
        List<String> expectedStrings =
            FileUtil.readLineBasedTextFromPath(expectedCsv.toPath()); // read expected csv lines
        List<String> actualStrings = FileUtil.readLineBasedTextFromPath(result);

        // all expected lines must be present
        assertThat(expectedStrings).isNotEmpty()
            .allSatisfy(expectedString -> assertThat(actualStrings).as(
                    "actual should contain expected string: %s",
                    expectedString)
                .contains(expectedString));

    }

    @NonNull
    private static Stream<Arguments> spssReportResults() {
        return Stream.of(
            Arguments.of(
                SpssReportType.KINDERGARTEN,
                "/CsvGenerator/expected/kindergarten.csv",
                CleanFileName.parse("Spss-Report-Kindergarten")),
            Arguments.of(
                SpssReportType.KLASSE_4,
                "/CsvGenerator/expected/klasse4.csv",
                CleanFileName.parse("Spss-Report-4_Primar")),
            Arguments.of(
                SpssReportType.KLASSE_8,
                "/CsvGenerator/expected/klasse8.csv",
                CleanFileName.parse("Spss-Report-2_Sek")),
            Arguments.of(
                SpssReportType.KLASSE_9,
                "/CsvGenerator/expected/klasse9.csv",
                CleanFileName.parse("Spss-Report-3_Sek")),
            Arguments.of(
                SpssReportType.FREMDSPRACHEN,
                "/CsvGenerator/expected/fremdsprache.csv",
                CleanFileName.parse("Spss-Report-Fremdsprache")),
            Arguments.of(
                SpssReportType.IMPF_ANLAESSE,
                "/CsvGenerator/expected/impfanlass.csv",
                CleanFileName.parse("Spss-Report-Impfanlässe"))
        );
    }

    @NonNull
    private static Stream<Arguments> csvReportResults() {
        return Stream.of(
            Arguments.of(
                CsvReportType.SCHULUNTERSUCHUNG,
                "/CsvGenerator/expected/schuluntersuchung.csv",
                CleanFileName.parse("CSV-Report-Schuluntersuchung"))
        );
    }

    @NonNull
    private static Stream<Arguments> csvImpfstoffReportResults() {
        return Stream.of(
            Arguments.of(
                CsvReportType.IMPFSTOFFE,
                "/CsvGenerator/expected/impfstoff.csv",
                CleanFileName.parse("CSV-Report-Impfstoffe"))
        );
    }
}
