/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv.EscadaCSVMapperService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv.EscadaCsvDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv.EscadaCsvService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.json.EscadaImportResultJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternalSyncService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternalSyncService;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternalSyncService;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternalSyncService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportPlanDTO;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createInputStreamFromPath;
import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromInputStream;
import static ch.dvbern.oss.hemed.esc.api.shared.externalsystem.HasExternalId.toUniqueExternalIdSet;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
public class EscadaService {

    public static final String ESCADA_SYNC_EVENT_ADDRESS = "escada-sync-event-address";
    private final AppClock appClock;
    private final EscadaCsvService escadaCsvService;
    private final EscadaSyncEventRepo escadaSyncEventRepo;
    private final ConfigService configService;
    private final FileStoreService fileStoreService;
    private final FileValidationService fileValidationService;
    private final SchuleExternalRepo schuleExternalRepo;
    private final SchuleExternalSyncService schuleExternalSyncService;
    private final KlasseExternalRepo klasseExternalRepo;
    private final KlasseExternalSyncService klasseExternalSyncService;
    private final LehrpersonExternalSyncService lehrpersonExternalSyncService;
    private final LehrpersonExternalRepo lehrpersonExternalRepo;
    private final KindExternalRepo kindExternalRepo;
    private final KindExternalSyncService kindExternalSyncService;
    private final KontaktPersonKindExternalSyncService kontaktPersonKindExternalSyncService;
    private final KontaktPersonKindExternalRepo kontaktPersonKindExternalRepo;
    private final EscadaMailService escadaMailService;
    private final TenantProducer tenantProducer;

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public EscadaImportResultJson processEscadaCsvUpload(@NonNull FileUpload fileUpload) {
        // check the filetype
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.ESCADA_SYNC);
        // store the sync data in a file
        EscadaSyncEvent escadaSyncEvent = this.saveEscadaSyncData(fileUpload);
        InputStream is = createInputStreamFromPath(fileUpload.filePath());
        return handleEscadaCSVUpload(is, escadaSyncEvent);
    }

    @ActivateRequestContext
    @ConsumeEvent(value = ESCADA_SYNC_EVENT_ADDRESS, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void handleEscadaSyncEvent(@NonNull EscadaSyncEvent escadaSyncEvent) {
        try {
            tenantProducer.setCurrentTenant(TenantEnum.BS); // Escada Import ist fix für BS
            var bytes = fileStoreService.getFileBytesSync(escadaSyncEvent.getFileData()
                .getFileInfoBucket()
                .getFileIdInBucket());
            if (bytes == null) {
                String msg = "ESCADA: File not found in S3 bucket: " + escadaSyncEvent.getFileData()
                    .getFileInfoBucket()
                    .getFileIdInBucket();
                LOG.error(msg);
                escadaMailService.sendErrorMail(msg);
                return;
            }
            this.handleEscadaCSVUpload(new ByteArrayInputStream(bytes), escadaSyncEvent);
        } catch (Exception e) {
            String msg = ExceptionUtils.getStackTrace(e);
            escadaMailService.sendErrorMail(msg);
        }
    }

    @NonNull
    private EscadaImportResultJson handleEscadaCSVUpload(
        @NonNull InputStream inputStream,
        @NonNull EscadaSyncEvent escadaSyncEvent
    ) {
        StopWatch stopWatch = StopWatch.createStarted();

        // Create a reader for the csv file and parse the content into a list of DTOs
        Reader reader = createReaderFromInputStream(inputStream);
        List<EscadaCsvDTO> escadaCsvDTOS = this.parseEscadaCsvAndRemoveUndesired(reader);

        // handle schulen
        var schuleResultItems = extractAndSyncSchulen(escadaCsvDTOS);

        // handle klassen
        var klassenResultItems = extractAndSyncKlassen(escadaCsvDTOS);

        // handle lehrpersonen
        var lehrpersonenResultItems = extractAndSyncLehrpersonen(escadaCsvDTOS);

        // handle Kinder
        var kinderResultItems = extractAndSyncKinder(escadaCsvDTOS);

        // handle kontaktpersonen kinder
        var kontaktPersonKindResultItems = extractAndSyncKontaktPersonKind(escadaCsvDTOS);
        var resultReport = new EscadaImportResultJson(
            escadaSyncEvent.getId(),
            appClock.localDateTime().now(),
            stopWatch.getDuration(),
            schuleResultItems,
            klassenResultItems,
            lehrpersonenResultItems,
            kinderResultItems,
            kontaktPersonKindResultItems
        );

        reportSummaryByMail(resultReport);
        LOG.info(resultReport.getImportSummary());
        return resultReport;
    }

    @NonNull
    List<ItemSyncResultJson<Schule>> extractAndSyncSchulen(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        ImportPlanDTO<SchuleExternal> importPlanDTO = extractSchulenAndDetermineUpdatePlan(escadaCsvDTOS);
        return schuleExternalSyncService.syncSchulen(importPlanDTO, ImportTyp.ESCADA);
    }

    @NonNull
    List<ItemSyncResultJson<Klasse>> extractAndSyncKlassen(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        ImportPlanDTO<KlasseExternal> importPlanDTO = extractKlassenAndDetermineUpdatePlan(escadaCsvDTOS);
        return klasseExternalSyncService.syncKlassen(importPlanDTO, ImportTyp.ESCADA);
    }

    @NonNull
    List<ItemSyncResultJson<Lehrperson>> extractAndSyncLehrpersonen(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        ImportPlanDTO<LehrpersonExternal> importPlanDTO =
            extractLehrpersonenAndDetermineUpdatePlan(escadaCsvDTOS);
        return lehrpersonExternalSyncService.syncLehrpersonen(importPlanDTO, ImportTyp.ESCADA);
    }

    @NonNull
    List<ItemSyncResultJson<Kind>> extractAndSyncKinder(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        ImportPlanDTO<KindExternal> importPlanDTO = extractKinderAndDetermineUpdatePlan(escadaCsvDTOS);
        return kindExternalSyncService.syncKinder(importPlanDTO, ImportTyp.ESCADA);
    }

    @NonNull
    List<ItemSyncResultJson<KontaktPersonKind>> extractAndSyncKontaktPersonKind(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        ImportPlanDTO<KontaktPersonKindExternal> importPlanDTO =
            extractKontaktPersonenKindAndDetermineUpdatePlan(escadaCsvDTOS);
        return kontaktPersonKindExternalSyncService.syncKontaktPersonKind(importPlanDTO, ImportTyp.ESCADA);
    }

    @NonNull
    private Set<SchuleExternal> extractUniqueExternalSchulen(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        return escadaCsvDTOS.stream()
            .map(EscadaCSVMapperService::toExternalSchule)
            .flatMap(Optional::stream) // unwrap the optional and only continue with the present values
            .collect(toUniqueExternalIdSet());
    }

    @NonNull
    private ImportPlanDTO<SchuleExternal> extractSchulenAndDetermineUpdatePlan(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        Set<SchuleExternal> importedItems = extractUniqueExternalSchulen(escadaCsvDTOS);
        LOG.debug("Extracted {} unique  schulen from the csv", importedItems.size());
        // now we want to check which of these schulen are already in the db, which are added and which are deleted
        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(schuleExternalRepo.findAllExternalIds());
        ImportPlanDTO<SchuleExternal> result = ImportPlanDTO.from(existingItems, importedItems);
        LOG.info(
            "Detected {} new schulen, {} deleted schulen and {} schulen to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private Set<KlasseExternal> extractUniqueExternalKlassen(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {
        return escadaCsvDTOS.stream()
            .map(EscadaCSVMapperService::toExternalKlasse)
            .flatMap(Optional::stream)
            .collect(toUniqueExternalIdSet());
    }

    @NonNull
    private ImportPlanDTO<KlasseExternal> extractKlassenAndDetermineUpdatePlan(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        Set<KlasseExternal> importedItems = extractUniqueExternalKlassen(escadaCsvDTOS);
        LOG.debug("Extracted {} unique klassen from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(klasseExternalRepo.findAllExternalIds());
        ImportPlanDTO<KlasseExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "ESCADA: Detected {} new klassen, {} deleted klassen and {} klassen to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private ImportPlanDTO<LehrpersonExternal> extractLehrpersonenAndDetermineUpdatePlan(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        Set<LehrpersonExternal> importedItems = extractUniqueExternalLehrpersonen(escadaCsvDTOS);
        LOG.debug("Extracted {} unique Lehrpersonen schule from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(lehrpersonExternalRepo.findAllExternalIds());
        ImportPlanDTO<LehrpersonExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "ESCADA: Detected {} new Lehrpersonen schule, {} deleted and {} to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private ImportPlanDTO<KindExternal> extractKinderAndDetermineUpdatePlan(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        Set<KindExternal> importedItems = extractUniqueExternalKinder(escadaCsvDTOS);
        LOG.debug("Extracted {} unique kinder from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(kindExternalRepo.findAllExternalIds());
        ImportPlanDTO<KindExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "ESCADA: Detected {} new kinder, {} deleted kinder and {} kinder to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private ImportPlanDTO<KontaktPersonKindExternal> extractKontaktPersonenKindAndDetermineUpdatePlan(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        Set<KontaktPersonKindExternal> importedItems = extractUniqueExternalKontaktPersonenKind(escadaCsvDTOS);
        LOG.debug("Extracted {} unique Lehrpersonen kinder from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(kontaktPersonKindExternalRepo.findAllExternalIds());
        ImportPlanDTO<KontaktPersonKindExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "ESCADA: Detected {} new Lehrpersonen kinder, {} deleted and {} to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    public List<EscadaCsvDTO> parseEscadaCsvAndRemoveUndesired(@NonNull Reader reader) {
        return escadaCsvService.parseEscadaCSV(reader);
    }

    private void reportSummaryByMail(@NonNull EscadaImportResultJson resultReport) {
        if (resultReport.hasErrors() && configService.isEscadaSyncErrorMailEnabled()) {
            // send mail
            LOG.error("ESCADA-CSV-Import FINISHED with errors: Sending error mail for the sync");
            escadaMailService.sendWarningMail(resultReport);
        } else {
            LOG.info("ESCADA-CSV-Import SUCCESSFUL");
        }
    }

    @NonNull
    protected EscadaSyncEvent saveEscadaSyncData(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.ESCADA_SYNC);
        // upload the file to S3
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileUpload, fileInfo);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Uploaded escada csv file  to S3: {}", putObjectResponse);

        LocalDate today = appClock.getToday();
        final String originalFileName = fileInfo.getFileName().getFileName();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            List.of(),
            today,
            "Escada-Sync",
            FileTyp.ESCADA_SYNC,
            originalFileName);

        FileData fileData = new FileData(FileTyp.ESCADA_SYNC, calculatedFileName, fileInfo);

        EscadaSyncEvent event = new EscadaSyncEvent(fileData);
        this.escadaSyncEventRepo.persist(event);
        return event;
    }

    @NonNull
    private Set<LehrpersonExternal> extractUniqueExternalLehrpersonen(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        // Liste aller KP erstellen (die im CSV flach als KP 1-4 vorliegen)
        List<LehrpersonExternal> list = escadaCsvDTOS.stream()
            // For each DTO, generate the persons in order
            .flatMap(dto -> Stream.of(
                    EscadaCSVMapperService.toExternalLehrperson(dto),
                    EscadaCSVMapperService.toExternalLehrperson2(dto),
                    EscadaCSVMapperService.toExternalLehrperson3(dto),
                    EscadaCSVMapperService.toExternalLehrperson4(dto)
                ).flatMap(Optional::stream) // Remove empty Optionals
            ).toList();

        // Map aller LehrerIds mit ihren Lehrer-Objekten (Duplikaten)
        Map<String, List<LehrpersonExternal>> lehrerIdToLehrer =
            list.stream().collect(Collectors.groupingBy(LehrpersonExternal::getExternalId));

        // Map aller LehrerIds mit ihren KlassenIds
        Map<String, Set<String>> lehrerIdToKlassenIds = new HashMap<>();
        for (Entry<String, List<LehrpersonExternal>> lehrerIdToKlassenEntry : lehrerIdToLehrer.entrySet()) {
            Set<String> klasseIds = lehrerIdToKlassenEntry.getValue().stream()
                .map(LehrpersonExternal::getEscadaKlasseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            lehrerIdToKlassenIds.put(lehrerIdToKlassenEntry.getKey(), klasseIds);
        }

        // Deduplicate Lehrer-Objekte: KlassenIds der Duplikate auf dem ersten Lehrer zusammenmergen
        Set<LehrpersonExternal> result = new HashSet<>();
        for (Entry<String, Set<String>> entry : lehrerIdToKlassenIds.entrySet()) {
            var lehrerToKeep = lehrerIdToLehrer.get(entry.getKey()).stream().findFirst().orElseThrow();
            Set<String> klassenForLehrer = entry.getValue();
            lehrerToKeep.setEscadaKlasseIds(String.join(",", klassenForLehrer));
            result.add(lehrerToKeep);
        }
        return result;
    }

    @NonNull
    Set<KindExternal> extractUniqueExternalKinder(@NonNull List<EscadaCsvDTO> escadaCsvDTOS) {

        Map<String, List<KindExternal>> kindIdsToEntries = escadaCsvDTOS.stream()
            .map(EscadaCSVMapperService::toExternalKind)
            .flatMap(Optional::stream)
            .collect(Collectors.groupingBy(KindExternal::getExternalId));

        // since our pseudo data is broken and has multiple entries for the same kind, we need to deduplicate
        // and keep one at random
        for (Entry<String, List<KindExternal>> kindIdToKinder : kindIdsToEntries.entrySet()) {
            Map<String, List<KindExternal>> duplicateIdKinder = kindIdToKinder.getValue().stream()
                .collect(
                    Collectors.groupingBy(e -> Objects.toString(e.getFullName(), ""))
                );
            if (duplicateIdKinder.size() > 1) {
                LOG.warn(
                    "ESCADA Found multiple entries for kind with externalId: '{}', keeping one at random",
                    kindIdToKinder.getKey());
                // first entry
                List<KindExternal> entriesOfFirstKind = duplicateIdKinder.values().stream()
                    .findFirst().orElseThrow();
                kindIdToKinder.setValue(entriesOfFirstKind);
            }
        }
        // now that we have a map of unique kind ids to entries, we can check if there are kinder with multiple klassen
        // that have Schule fuer Gestaltung as schule, if this is the case we drop the entry
        for (Entry<String, List<KindExternal>> stringListEntry : kindIdsToEntries.entrySet()) {
            List<KindExternal> kindExternals = stringListEntry.getValue();
            if (kindExternals.size() > 1) {
                // check if the schule is schule fuer gestaltung and drop that specific entry if it is
                kindExternals.removeIf(e -> {
                    if (e.getEscadaKlasseExternal() != null) {
                        boolean isSfG = e.getEscadaKlasseExternal().isSchuleFuerGestaltung();
                        if (isSfG) {
                            LOG.info(
                                "Dropping Klasse Entry for Kind with externalId: '{}', because it belongs to Schule "
                                    + "fuer Gestaltung",
                                e.getExternalId());
                        }
                        return isSfG;
                    }
                    return false;
                });
            }
        }

        // print out if there are still entries that have multiple klassen
        kindIdsToEntries.entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> LOG.warn(
                "Kind with externalId: '{}' has multiple klassen, keeping one at random",
                e.getKey()));

        return kindIdsToEntries.values().stream()
            .flatMap(List::stream)
            .collect(toUniqueExternalIdSet());
    }

    @NonNull
    private Set<KontaktPersonKindExternal> extractUniqueExternalKontaktPersonenKind(
        @NonNull List<EscadaCsvDTO> escadaCsvDTOS
    ) {
        // Liste aller KP erstellen (die im CSV flach als KP 1-2 vorliegen), i.e. 'unflatten'
        List<KontaktPersonKindExternal> list = escadaCsvDTOS.stream()
            // For each DTO, generate the persons in order
            .flatMap(dto -> Stream.of(
                    EscadaCSVMapperService.toExternalKontaktPersonKind(dto),
                    EscadaCSVMapperService.toExternalKontaktPersonKind2(dto)
                ).flatMap(Optional::stream) // Remove empty Optionals
            ).toList();

        // Map aller ElternIds mit ihren Eltern-Objekten (Duplikaten)
        Map<String, List<KontaktPersonKindExternal>> kpIdToKps =
            list.stream().collect(Collectors.groupingBy(KontaktPersonKindExternal::getExternalId));

        // Map aller ElternIds mit ihren SchuelerIds (i.e. ihren Kindern)
        Map<String, Set<String>> kpIdToSchuelerIds = new HashMap<>();
        for (Entry<String, List<KontaktPersonKindExternal>> kpIdToKpsEntry : kpIdToKps.entrySet()) {
            Set<String> schuelerIds = kpIdToKpsEntry.getValue().stream()
                .map(KontaktPersonKindExternal::getSchuelerId)
                .collect(Collectors.toSet());
            kpIdToSchuelerIds.put(kpIdToKpsEntry.getKey(), schuelerIds);
        }

        // Deduplicate Kind-Objekte: schuelerIds der Duplikate auf der ersten KP zusammenmergen
        Set<KontaktPersonKindExternal> result = new HashSet<>();
        for (Entry<String, Set<String>> entry : kpIdToSchuelerIds.entrySet()) {
            var lehrerToKeep = kpIdToKps.get(entry.getKey()).stream().findFirst().orElseThrow();
            Set<String> schuelerForKp = entry.getValue();
            lehrerToKeep.setSchuelerIds(String.join(",", schuelerForKp));
            result.add(lehrerToKeep);
        }
        return result;
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public void writeCsv(String name, List<EscadaCsvDTO> fixedDTOs) {
        escadaCsvService.writeCsv(name, fixedDTOs);
    }
}


