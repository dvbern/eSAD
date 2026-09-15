/*
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

package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfstatusKrankheitBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenDataMinimalBuilder;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KinderdossierUntersuchungImpfungenUtilTest {

    private final FileStoreService fileStoreServiceMock = mock(FileStoreService.class);

    @FunctionalInterface
    private interface PrintAction {
        void print(BasePrinter basePrinter, Document document);
    }

    @NonNull
    private String renderPdfAndExtractText(@NonNull PrintAction action) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            BasePrinter basePrinter = new BasePrinter(AppLanguages.DE, TenantEnum.BS, fileStoreServiceMock);
            Document document = basePrinter.getDocument(byteArrayOutputStream);
            action.print(basePrinter, document);
            document.close();
            return extractPdfText(byteArrayOutputStream.toByteArray());
        }
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

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<KinderdossierUntersuchungImpfungenUtil> constructor =
            KinderdossierUntersuchungImpfungenUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        KinderdossierUntersuchungImpfungenUtil instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    void printImpfungenHeader_allVisible_withUntersuchungData() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setShowImpfungenHeaderImpfausweisUnvollstaendig(true);
        config.setShowImpfungenHeaderImpfausweisNichtVorhanden(true);
        config.setShowImpfungenHeaderImpfausweisZweiPlusEinsSchema(true);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(builder -> builder
            .forUntersuchung(untersuchung)
            .withImpfausweisUnvollstaendig(true)
            .withImpfausweisNichtVorhanden(false)
            .withImpfausweisZweiPlusEinsSchema(true));
        untersuchung.setUntersuchungImpfungen(impfungen);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenHeader(config, untersuchung, basePrinter, doc));

        assertThat(text).contains("Impfausweis unvollständig")
            .contains("Kein Impfausweis?")
            .contains("2+1 Schema?")
            .contains("Ja")
            .contains("Nein");
    }

    @Test
    void printImpfungenHeader_allHidden() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setShowImpfungenHeaderImpfausweisUnvollstaendig(false);
        config.setShowImpfungenHeaderImpfausweisNichtVorhanden(false);
        config.setShowImpfungenHeaderImpfausweisZweiPlusEinsSchema(false);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenHeader(config, untersuchung, basePrinter, doc));

        assertThat(text).doesNotContain("Impfausweis unvollständig")
            .doesNotContain("Kein Impfausweis?")
            .doesNotContain("2+1 Schema?");
    }

    @Test
    void printImpfungenDataMinimal_allVisible_withData() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setImpfungenDataMinimalImpfausweisGeprueft(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalImmunsuppression(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalMasernImpfstatus(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalMasernAnzahlImpfungen(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalHpvImpfstatus(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalHpvAnzahlImpfungen(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalImpfempfehlung(RequirementSetting.OPTIONAL);
        config.setImpfungenDataMinimalImpfungDurchgefuehrt(RequirementSetting.OPTIONAL);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(builder -> builder
            .forUntersuchung(untersuchung));
        untersuchung.setUntersuchungImpfungen(impfungen);

        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(builder -> builder
            .forUntersuchungImpfungen(impfungen)
            .withImpfausweisGeprueft(ImpfausweisGeprueft.JA, null)
            .withImmunsuppression(Immunsuppression.NEIN, null)
            .withMasernImpfstatus(ImpfStatusMinimal.VOLLSTAENDIG, null)
            .withMasernAnzahlImpfungen(2)
            .withHpvImpfstatus(ImpfStatusMinimal.UNVOLLSTAENDIG, null)
            .withHpvAnzahlImpfungen(1)
            .withImpfempfehlung(ImpfempfehlungMinimal.JA, null)
            .withImpfungDurchgefuehrt(ImpfungDurchgefuehrtMinimal.NEIN, null));
        impfungen.setUntersuchungImpfungenDataMinimal(dataMinimal);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDataMinimal(config, untersuchung, basePrinter, doc));

        assertThat(text).contains("Impfausweis geprüft:")
            .contains("Immunsuppression:")
            .contains("Masern: Impfstatus:")
            .contains("Masern:")
            .contains("HPV: Impfstatus:")
            .contains("HPV:")
            .contains("Impfempfehlung")
            .contains("Impfung")
            .contains("Vollständig")
            .contains("Unvollständig")
            .contains("2")
            .contains("1")
            .contains("Ja")
            .contains("Nein");
    }

    @Test
    void printImpfungenDataMinimal_allHidden() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setImpfungenDataMinimalImpfausweisGeprueft(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalImmunsuppression(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalMasernImpfstatus(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalMasernAnzahlImpfungen(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalHpvImpfstatus(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalHpvAnzahlImpfungen(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalImpfempfehlung(RequirementSetting.NOT_VISIBLE);
        config.setImpfungenDataMinimalImpfungDurchgefuehrt(RequirementSetting.NOT_VISIBLE);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDataMinimal(config, untersuchung, basePrinter, doc));

        assertThat(text).doesNotContain("Impfausweis geprüft:")
            .doesNotContain("Immunsuppression:")
            .doesNotContain("Masern: Impfstatus:")
            .doesNotContain("HPV: Impfstatus:");
    }

    @Test
    void printTableImpfstatus_withEntries() throws IOException {
        UntersuchungImpfstatusKrankheit roeteln = UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder
            .forKrankheit(Krankheit.ROETELN)
            .withAnzahlImpfungen(2)
            .withDatumLetzteImpfung(LocalDate.of(2023, 5, 20))
            .withImpfschutzGegeben(ImpfschutzGegeben.JA));
        roeteln.setImpfempfehlung(false);
        roeteln.setImpfentscheid(true);

        UntersuchungImpfstatusKrankheit diphtherie = UntersuchungImpfstatusKrankheitBuilder.create(builder -> builder
            .forKrankheit(Krankheit.DIPHTERIE)
            .withAnzahlImpfungen(3)
            .withDatumLetzteImpfung(LocalDate.of(2022, 11, 10))
            .withImpfschutzGegeben(ImpfschutzGegeben.JA));
        diphtherie.setImpfempfehlung(true);
        diphtherie.setImpfentscheid(false);

        List<UntersuchungImpfstatusKrankheit> list = List.of(roeteln, diphtherie);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printTableImpfstatus(list, basePrinter, doc));

        assertThat(text).contains("Impfstatus vor allfälligen Impfungen")
            .contains("Krankheit")
            .contains("Diphtherie")
            .contains("Röteln")
            .contains("11/2022")
            .contains("05/2023")
            .contains("Ja")
            .contains("Nein");
    }

    @Test
    void printImpfungenTableImpfliste_withEntries() throws IOException {
        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(b -> b.forUntersuchung(untersuchung));

        Benutzer arzt = BenutzerBuilder.create(b -> b
            .withVorname("Hans")
            .withNachname("Muster")
            .withTitel("Dr. med."));

        Benutzer mpa = BenutzerBuilder.create(b -> b
            .withVorname("Anna")
            .withNachname("Meier"));

        Impfstoff priorix = ImpfstoffBuilder.create(b -> b
            .withName("Priorix")
            .forKrankheiten(Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN));

        ImpfstoffCharge charge = ImpfstoffChargeBuilder.create(b -> b
            .withName("LOT12345")
            .forImpfstoff(priorix));

        UntersuchungImpfungIntern impfung1 = UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(impfungen)
            .withImpfstoff(priorix)
            .withCharge(charge)
            .withDatumImpfung(LocalDate.of(2025, 2, 10))
            .withUserErfasst(mpa)
            .withUserDurchgefuehrt(arzt)
            .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
            .withImmunitaetForKrankheit(Krankheit.MASERN, true)
            .withImmunitaetForKrankheit(Krankheit.MUMPS, true)
            .withImmunitaetForKrankheit(Krankheit.ROETELN, false));

        Impfstoff boostrix = ImpfstoffBuilder.create(b -> b
            .withName("Boostrix")
            .forKrankheiten(Krankheit.DIPHTERIE, Krankheit.TETANUS));

        UntersuchungImpfungIntern impfung2 = UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(impfungen)
            .withImpfstoff(boostrix)
            .withCharge(null)
            .withDatumImpfung(null)
            .withUserErfasst(mpa)
            .withUserDurchgefuehrt(arzt)
            .withImpfStatus(ImpfStatus.NICHT_DURCHGEFUEHRT));

        List<UntersuchungImpfungIntern> list = List.of(impfung1, impfung2);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenTableImpfliste(list, basePrinter, doc));

        assertThat(text).contains("Impfliste")
            .contains("Impfstoff")
            .contains("Priorix")
            .contains("Boostrix")
            .contains("LOT12345")
            .contains("10.02.2025")
            .contains(arzt.getNachname())
            .contains(mpa.getNachname())
            .contains("Masern: Ja")
            .contains("Mumps: Ja")
            .contains("Röteln: Nein")
            .contains("Ja")
            .contains("Nein");
    }

    @Test
    void printImpfungenCheckliste_allVisible_withData() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setCheckEinwilligungVorhanden(RequirementSetting.OPTIONAL);
        config.setCheckKontraindikationenAbgeklaert(RequirementSetting.OPTIONAL);
        config.setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(RequirementSetting.OPTIONAL);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(builder -> builder
            .forUntersuchung(untersuchung)
            .withCheckEinwilligungVorhanden(true)
            .withCheckKontraindikationenAbgeklaert(false)
            .withCheckDoppelkontrolleImpfentscheidDurchgefuehrt(true));
        untersuchung.setUntersuchungImpfungen(impfungen);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenCheckliste(config, untersuchung, basePrinter, doc));

        assertThat(text).contains("Ist das Einverständnis vorhanden?")
            .contains("Sind die Kontraindikationen abgeklärt?")
            .contains("Doppelkontrolle Impfentscheid gemacht?")
            .contains("Ja")
            .contains("Nein");
    }

    @Test
    void printImpfungenCheckliste_allHidden() throws IOException {
        AnlassConfig config = AnlassConfigBuilder.defaultAnlassConfig();
        config.setCheckEinwilligungVorhanden(RequirementSetting.NOT_VISIBLE);
        config.setCheckKontraindikationenAbgeklaert(RequirementSetting.NOT_VISIBLE);
        config.setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(RequirementSetting.NOT_VISIBLE);

        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenCheckliste(config, untersuchung, basePrinter, doc));

        assertThat(text).doesNotContain("Ist das Einverständnis vorhanden?")
            .doesNotContain("Sind die Kontraindikationen abgeklärt?")
            .doesNotContain("Doppelkontrolle Impfentscheid gemacht?");
    }

    @Test
    void printImpfungenDoppelkontrolle_withVisumData() throws IOException {
        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);

        Benutzer person1 = BenutzerBuilder.create(b -> b
            .withVorname("Eva")
            .withNachname("Keller")
            .withTitel("Dr. med."));

        Benutzer person2 = BenutzerBuilder.create(b -> b
            .withVorname("Peter")
            .withNachname("Schmid"));

        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(builder -> builder
            .forUntersuchung(untersuchung)
            .withVisum1(person1, now)
            .withVisum2(person2, now));
        untersuchung.setUntersuchungImpfungen(impfungen);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDoppelkontrolle(untersuchung, basePrinter, doc));

        assertThat(text).contains("Doppelkontrolle Visum 1:")
            .contains("Doppelkontrolle Visum 2:")
            .contains(PrintUtil.trimToPlaceholder(person1))
            .contains(PrintUtil.trimToPlaceholder(person2));
    }

    @Test
    void printImpfungenDoppelkontrolle_withNullVisum() throws IOException {
        LocalDate now = LocalDate.of(2025, 1, 15);
        Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(now);
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(builder -> builder
            .forUntersuchung(untersuchung)
            .withVisum1(null, null)
            .withVisum2(null, null));
        untersuchung.setUntersuchungImpfungen(impfungen);

        String text = renderPdfAndExtractText((basePrinter, doc) ->
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDoppelkontrolle(untersuchung, basePrinter, doc));

        assertThat(text).contains("Doppelkontrolle Visum 1:")
            .contains("Doppelkontrolle Visum 2:")
            .contains("-");
    }

}
