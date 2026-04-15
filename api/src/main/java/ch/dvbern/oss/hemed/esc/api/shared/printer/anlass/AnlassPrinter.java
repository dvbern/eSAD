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

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@NoTransactionService
public final class AnlassPrinter {

    private AnlassPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void print(
        @NonNull AnlassPrintDTO dto,
        @NonNull OutputStream outputStream,
        @NonNull FileStoreService fileStoreService,
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull ConfigService configService
    ) {
        BasePrinter basePrinter = new BasePrinter(dto.getLanguage(), dto.getTenant(), fileStoreService);

        Anlass anlass = dto.getAnlass();
        Document document = basePrinter.getDocument(outputStream, PageSize.A4.rotate());
        basePrinter.addHeader(document);

        // Anlass-Titel
        var tl = new TLProducer(dto::getLanguage, dto::getTenant).forAppLanguage(dto.getLanguage());
        basePrinter.addH1Title(anlass.getFullAnlassTitel(tl), document);

        // Footer
        basePrinter.addPaginationFooter(
            I18nMessage.of(
                "printer.anlass.footer",
                "titel",
                anlass.getTitel(),
                "datum",
                PrintUtil.trimToPlaceholder(anlass.getDatum()),
                "now",
                PrintUtil.trimToPlaceholder(dto.getNow())),
            document);

        // Add Anlass data section
        addAnlassDaten(anlass, basePrinter, document, dto);

        // Add information about the Anlass
        addAnlassInformationen(anlass, basePrinter, document);

        // Add list of invited kinder
        boolean printEsadId = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getEsadIdFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());

        addEingeladeneKinder(dto.getEinladungen(), dto.getNachkontrollen(), basePrinter, document, printEsadId);

        document.close();
    }

    private static void addAnlassDaten(
        @NonNull Anlass anlass,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document,
        @NonNull AnlassPrintDTO dto
    ) {
        basePrinter.addH2Title(I18nMessage.of("printer.anlass.daten"), document);

        // Add Schulärztin
        basePrinter.addKeyValue(
            "printer.anlass.arzt",
            PrintUtil.trimToPlaceholder(anlass.getArzt() != null ? anlass.getArzt().getNameVorname() : null),
            document);

        // Add MPA
        basePrinter.addKeyValue(
            "printer.anlass.mpa",
            PrintUtil.trimToPlaceholder(anlass.getMpa() != null ? anlass.getMpa().getNameVorname() : null),
            document);

        // Add Untersuchungs-Datum
        basePrinter.addKeyValue("printer.anlass.datum", PrintUtil.trimToPlaceholder(anlass.getDatum()), document);

        // Add Uhrzeit
        if (anlass.getZeitVonBis() != null) {
            basePrinter.addKeyValue(
                "printer.anlass.zeit",
                PrintUtil.formatLocalDateTime(anlass.getZeitVonBis().getVon()) + " - " +
                    PrintUtil.formatLocalDateTime(anlass.getZeitVonBis().getBis()),
                document);
        } else {
            basePrinter.addKeyValue("printer.anlass.zeit", PrintUtil.PLACEHOLDER, document);
        }

        // These will be populated in the service
        // Add Schule/n
        basePrinter.addKeyValue(
            "printer.anlass.schulen",
            PrintUtil.trimToPlaceholder(dto.getSchulenCommaSeparated()),
            document);
        // Add Klasse/n
        basePrinter.addKeyValue(
            "printer.anlass.klassen",
            PrintUtil.trimToPlaceholder(dto.getKlassenCommaSeparated()),
            document);
        // Add Lehrperson/en
        basePrinter.addKeyValue(
            "printer.anlass.lehrpersonen",
            PrintUtil.trimToPlaceholder(dto.getLehrpersonenCommaSeparated(basePrinter)),
            document
        );
    }

    private static void addAnlassInformationen(
        @NonNull Anlass anlass,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH2Title(I18nMessage.of("printer.anlass.informationen"), document);

        // Add Info Schularzt/Schulärztin
        basePrinter.addKeyValue(
            "printer.anlass.arzt.info",
            PrintUtil.trimToPlaceholder(anlass.getArztInfo()), document);

        // Add Info MPA
        basePrinter.addKeyValue(
            "printer.anlass.mpa.info",
            PrintUtil.trimToPlaceholder(anlass.getMpaInfo()), document);

        // Add Info Orthoptist/Orthoptistin
        basePrinter.addKeyValue(
            "printer.anlass.weitere.info",
            PrintUtil.trimToPlaceholder(anlass.getWeiterePersonInfo()), document);

        // Add PageBreak
        basePrinter.addPageBreak(document, true);
    }

    private static void addEingeladeneKinder(
        @NonNull List<Einladung> einladungen,
        @Nullable List<Nachkontrolle> nachkontrollen,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document,
        @IgnoreForIdClassTest boolean printEsadId
    ) {
        basePrinter.addH2Title(I18nMessage.of("printer.anlass.eingeladene.kinder"), document);

        // Create table headers
        List<I18nMessage> headers = new ArrayList<>();
        headers.add(I18nMessage.of("printer.anlass.kind.name"));
        headers.add(I18nMessage.of("printer.anlass.kind.vorname"));
        headers.add(I18nMessage.of("printer.anlass.kind.geschlecht"));
        headers.add(I18nMessage.of("printer.anlass.kind.geburtsdatum"));
        if (printEsadId) {
            headers.add(I18nMessage.of("printer.anlass.kind.esadId"));
        }
        headers.add(I18nMessage.of("printer.anlass.kind.klassenstufe"));
        headers.add(I18nMessage.of("printer.anlass.kind.nachkontrolle"));
        headers.add(I18nMessage.of("printer.anlass.kind.status"));
        headers.add(I18nMessage.of("printer.anlass.kind.abmeldegrund"));
        headers.add(I18nMessage.of("printer.anlass.kind.impfausweis"));
        headers.add(I18nMessage.of("printer.anlass.kind.fragebogen"));

        // Create table rows
        List<List<String>> rows = new ArrayList<>();
        for (Einladung einladung : einladungen) {
            Kind kind = einladung.getDossier().getKind();
            Nachkontrolle nachkontrolleOfKind = nachkontrollen != null ?
                nachkontrollen.stream()
                    .filter(nk -> nk.getDossier().getId().equals(einladung.getDossier().getId()))
                    .findFirst()
                    .orElse(null) :
                null;
            List<String> row = new ArrayList<>();
            row.add(PrintUtil.trimToPlaceholder(kind.getName()));
            row.add(PrintUtil.trimToPlaceholder(kind.getVorname()));
            row.add(basePrinter.translateValue(kind.getGeschlecht()));
            row.add(PrintUtil.trimToPlaceholder(kind.getGeburtsdatum()));

            if (printEsadId) {
                row.add(
                    PrintUtil.replaceWhitespaceWithNonBreakingSpace(PrintUtil.trimToPlaceholder(kind.getEsadId()))
                );
            }

            Klassenstufe klassenstufe = kind.getKlasse() != null ? kind.getKlasse().getKlassenstufe() : null;
            row.add(PrintUtil.replaceWhitespaceWithNonBreakingSpace(
                PrintUtil.trimToPlaceholder(basePrinter.translateValue(klassenstufe))));
            // Check if nachkontrolle is set in UntersuchungDurchfuehrung
            boolean nachkontrolle = (einladung.getUntersuchung() != null
                && einladung.getUntersuchung().getUntersuchungDurchfuehrung() != null)
                ? einladung.getUntersuchung().getUntersuchungDurchfuehrung().isNachkontrolle()
                : (nachkontrolleOfKind != null && nachkontrolleOfKind.getAnlassFlags().hasAnyNachkontrolle());
            row.add(basePrinter.translateValue(nachkontrolle));
            row.add(basePrinter.translateValue(einladung.getEinladungStatus()));
            row.add(basePrinter.translateValue(einladung.getGrundAbmeldung()));
            row.add(basePrinter.translateValue(isNotizWithFiles(einladung.getImpfausweisNotiz().orElse(null))));
            row.add(basePrinter.translateValue(isNotizWithFiles(einladung.getFragebogenNotiz().orElse(null))));
            rows.add(row);
        }

        // Add table to document
        basePrinter.addTable(headers, rows, document, true);
        basePrinter.addValue(I18nMessage.of("printer.anlass.abbrevation.Nachkontrolle"), false, document);
        basePrinter.addValue(I18nMessage.of("printer.anlass.abbrevation.Impfausweis"), false, document);
        basePrinter.addValue(I18nMessage.of("printer.anlass.abbrevation.Fragebogen"), false, document);
    }

    private static boolean isNotizWithFiles(@Nullable Notiz notiz) {
        if (notiz == null) {
            return false;
        }
        return !notiz.getFiles().isEmpty();
    }
}
