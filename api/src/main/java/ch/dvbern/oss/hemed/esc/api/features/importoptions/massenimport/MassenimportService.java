/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.MassenimportRollbackException;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportAsyncParams;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportErrorDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportReportDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportResultDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportRowDTO;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KindSaveModeDuplikat;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jsoup.helper.Validate;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.getLastNonEmptyRowIndex;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.getSheetFromFileUpload;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.MassenimportUtil.isSheetStructureValid;

@Service
@Slf4j
@AllArgsConstructor
public class MassenimportService {

    private final EventBus eventBus;
    private final MassenimportDTOHelperService massenimportDTOHelper;
    private final MassenimportMailService massenimportMailService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final KindService kindService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final BenutzerService benutzerService;
    private final AppClock appClock;
    private final TenantProducer tenantProducer;
    static final String MASSENIMPORT_EVENT = "massenimport-event";
    private static final String MASSENIMPORT_TEMPLATE = "/files/massenimport/massenimport_standard.xlsx";
    private static final String MASSENIMPORT_TEMPLATE_FILENAME = "massenimport_standard_vorlage.xlsx";
    private static final String MASSENIMPORT_TEMPLATE_WITH_KLASSE = "/files/massenimport/massenimport_klassen.xlsx";
    private static final String MASSENIMPORT_TEMPLATE_WITH_KLASSE_FILENAME = "massenimport_klassen_vorlage.xlsx";

    public PrintResultDTO getTemplateFile(boolean standardTemplate) {
        String filePath = standardTemplate ? MASSENIMPORT_TEMPLATE : MASSENIMPORT_TEMPLATE_WITH_KLASSE;
        String fileName =
            standardTemplate ? MASSENIMPORT_TEMPLATE_FILENAME : MASSENIMPORT_TEMPLATE_WITH_KLASSE_FILENAME;
        try (InputStream inputStream = BasePrinter.class.getResourceAsStream(filePath)) {
            Objects.requireNonNull(inputStream, filePath + " was not found");

            byte[] result = IOUtils.toByteArray(inputStream);

            return new PrintResultDTO(
                result,
                FileDownloadUtil.buildFileInfo(
                    fileName,
                    FileDownloadUtil.MIME_TYPE_XLSX,
                    result.length
                ));
        } catch (IOException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not load the template for the mass-import"), e);
        }
    }

    public void validateFileStructureThenProcessFileAsync(
        @NonNull FileUpload fileUpload,
        @NonNull ID<Schule> schuleID,
        @Nullable ID<Klasse> klasseID,
        boolean standardImport
    ) {
        Sheet sheet = getSheetFromFileUpload(fileUpload);

        if (!isSheetStructureValid(sheet, standardImport)) {
            throw new AppValidationException(AppValidationMessage.invalidMassenuploadFile());
        }
        Benutzer benutzer = benutzerService.getCurrentBenutzerOrThrowException();
        eventBus.send(
            MASSENIMPORT_EVENT, new MassenimportAsyncParams(
                sheet,
                schuleID,
                klasseID,
                standardImport,
                benutzer,
                tenantProducer.produceTenant()
            ));

    }

    @Transactional(TxType.REQUIRED)
    public void processMassenimportFileForSchuleAndKlasse(
        @NonNull Sheet sheet,
        @NonNull ID<Schule> schuleID,
        @Nullable ID<Klasse> klasseID,
        boolean standardImport,
        @NonNull Benutzer benutzer
    ) {
        Map<Integer, MassenimportRowDTO> data = new HashMap<>();
        int lastNonEmptyRowIndex = getLastNonEmptyRowIndex(sheet);

        for (int i = 2; i <= lastNonEmptyRowIndex; i++) {
            Row row = sheet.getRow(i);
            MassenimportRowDTO dto = massenimportDTOHelper.mapRowToRowDTO(i, row, schuleID, standardImport);
            data.put(i, dto);
        }

        int totalRows = lastNonEmptyRowIndex - 1; // -2 for header and +1 for 0-based-indexing = -1
        MassenimportResultDTO resultDTO = processData(data, schuleID, klasseID, totalRows, standardImport);
        LOG.info(
            "Massenimport:\n {} rows processed\n {} rows skipped\n {} errors found",
            totalRows,
            resultDTO.getSkippedRows(),
            resultDTO.getErrorsWhilePreparing().size()
        );

        resultDTO.getErrorsWhilePreparing()
            .stream()
            .filter(Objects::nonNull)
            .forEach(error -> LOG.warn("Massenimport: prepare error: {}", error));

        persistResults(resultDTO);

        reportSummaryByEmail(massenimportDTOHelper.mapResultToReportDTO(resultDTO), benutzer);
        // make sure we rollback the transaction
        if (!resultDTO.couldImportAllRows()) {
            throw new MassenimportRollbackException(
                resultDTO.getSkippedRows(),
                resultDTO.getExceptionsWhilePersisting());
        }

    }

    @SuppressWarnings({
        "java:S3776", /* 16 Instead of 15 Cognitive Complexity is alright for readability */
        "java:S2637" /*  Validate.notNull takes care of possible null-value*/
    })
    public MassenimportResultDTO processData(
        @NonNull Map<Integer, MassenimportRowDTO> data,
        @NonNull ID<Schule> schuleID,
        @Nullable ID<Klasse> klasseID,
        @NonNull Integer totalRows,
        boolean standardImport
    ) {
        int skippedRows = 0;

        List<Kind> kinder = new ArrayList<>();
        List<KontaktPersonKind> kontaktPersonenKind = new ArrayList<>();
        List<Klasse> klassen = new ArrayList<>();
        List<MassenimportErrorDTO> errors = new ArrayList<>();

        for (Entry<Integer, MassenimportRowDTO> entry : data.entrySet()) {
            MassenimportRowDTO dto = entry.getValue();
            Schule schule = schuleService.getById(schuleID);
            Klasse klasse = null;

            if (standardImport) {
                Validate.notNull(klasseID, "klasseID param must not be null for standard import");
                klasse = klasseService.getById(klasseID);
            } else {
                klasse = entry.getValue().getKlasse().getValue();
            }

            Kind kind = mapMassenuploadRowDTOToKind(dto, schule, klasse, appClock.offsetDateTime().now());

            // collect errors
            dto.getAllErrors().stream().filter(Objects::nonNull).forEach(errors::add);

            if (!dto.hasCriticalErrors() && kind != null) {
                kinder.add(kind);
                KontaktPersonKind kpKind =
                    mapMassenuploadRowDTOToKontaktPersonKind(dto, kind, appClock.offsetDateTime().now());
                if (kpKind != null) {
                    kontaktPersonenKind.add(kpKind);
                }
            } else {
                skippedRows++;
            }
        }

        return new MassenimportResultDTO(
            totalRows,
            skippedRows,
            kinder,
            kontaktPersonenKind,
            klassen,
            errors,
            new ArrayList<>()
        );
    }

    public void persistResults(@NonNull MassenimportResultDTO resultDTO) {
        try {
            List<Kind> kinder = resultDTO.getImportedKinder()
                .stream()
                .map(kind -> kindService.persistWithDuplicateCheck(kind, null, KindSaveModeDuplikat.CHECK))
                .toList();
            List<KontaktPersonKind> kontaktPersonen = resultDTO.getImportedKontaktPersonenKind()
                .stream()
                .map(kontaktPersonKindService::persist)
                .toList();
            resultDTO.setImportedKinder(kinder);
            resultDTO.setImportedKontaktPersonenKind(kontaktPersonen);
            LOG.info(
                "Massenimport: Persisted {} Kinder and {} KontaktPersonenKind",
                resultDTO.getImportedKinder().size(),
                resultDTO.getImportedKontaktPersonenKind().size());
        } catch (Exception e) {
            LOG.error(
                "Massenimport: An error occured while persisting the result of the Mass-import, stopping import",
                e);
            resultDTO.getExceptionsWhilePersisting().add(e); // this will only ever be one
        }
    }

    public void reportSummaryByEmail(@NonNull MassenimportReportDTO reportDTO, @NonNull Benutzer benutzer) {
        if (reportDTO.isSuccess()) {
            massenimportMailService.sendSuccessMail(reportDTO, benutzer);
        } else {
            massenimportMailService.sendFailureMail(reportDTO, benutzer);
        }
    }

    @Nullable
    public Kind mapMassenuploadRowDTOToKind(
        @NonNull MassenimportRowDTO rowDTO,
        @NonNull Schule schule,
        @Nullable Klasse klasse,
        @NonNull OffsetDateTime now
    ) {
        if (!rowDTO.hasImportableKindData()) {
            return null;
        }
        String name = Objects.requireNonNull(rowDTO.getNachname().getValue());
        String vorname = Objects.requireNonNull(rowDTO.getVorname().getValue());
        Geschlecht geschlecht = Objects.requireNonNull(rowDTO.getGeschlecht().getValue());
        LocalDate geburtsdatum = Objects.requireNonNull(rowDTO.getGeburtsdatum().getValue());

        return new Kind(
            new ExternalImportInfo(
                null,
                true,
                now,
                now,
                ImportTyp.EXCEL_MASSENIMPORT
            ),
            null,
            "SetInListener", // will be set in AbstractEntityListener
            rowDTO.getSozialversicherungsNummer().getValue(),
            name,
            vorname,
            "TODO", // will be set in AbstractEntityListener
            "TODO", // will be set in AbstractEntityListener
            geschlecht,
            geburtsdatum,
            rowDTO.getNationalitaet().getValue(),
            rowDTO.getErstsprache().getValue(),
            rowDTO.getWohntyp().getValue(),
            new Adresse(
                rowDTO.getStrasseUndNr().getValue(),
                rowDTO.getAdresszusatz().getValue(),
                rowDTO.getPlz().getValue(),
                rowDTO.getOrt().getValue(),
                rowDTO.getLand().getValue()
            ),
            rowDTO.getEmail().getValue(),
            rowDTO.getTelefon().getValue(),
            schule,
            klasse,
            EntityAktivStatus.AKTIV
        );
    }

    @Nullable
    public KontaktPersonKind mapMassenuploadRowDTOToKontaktPersonKind(
        @NonNull MassenimportRowDTO rowDTO,
        @NonNull Kind kind,
        @NonNull OffsetDateTime now
    ) {
        if (!rowDTO.hasImportableKontaktpersonKindData()) {
            return null;
        }
        return new KontaktPersonKind(
            new ExternalImportInfo(
                null,
                true,
                now,
                now,
                ImportTyp.EXCEL_MASSENIMPORT
            ),
            Set.of(kind),
            Objects.requireNonNull(rowDTO.getNachnameKp().getValue()),
            rowDTO.getVornameKp().getValue(),
            Objects.requireNonNull(rowDTO.getGeschlechtKp().getValue()),
            rowDTO.getBeziehungKp().getValue(),
            new Adresse(
                rowDTO.getStrasseUndNrKp().getValue(),
                rowDTO.getAdresszusatzKp().getValue(),
                rowDTO.getPlzKp().getValue(),
                rowDTO.getOrtKp().getValue(),
                rowDTO.getLandKp().getValue()
            ),
            rowDTO.getEmail().getValue(),
            rowDTO.getTelefonKp().getValue(),
            Boolean.TRUE.equals(rowDTO.getKorrespondenzKp().getValue())
        );
    }
}
