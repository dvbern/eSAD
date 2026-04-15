package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

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
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv.PupilCSVMapperService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv.PupilCsvDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv.PupilCsvService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.json.PupilImportResultJson;
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
public class PupilService {

    public static final String PUPIL_SYNC_EVENT_ADDRESS = "pupil-sync-event-address";
    private final AppClock appClock;
    private final FileValidationService fileValidationService;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final PupilSyncEventRepo pupilSyncEventRepo;
    private final PupilCsvService pupilCsvService;
    private final SchuleExternalRepo schuleExternalRepo;
    private final SchuleExternalSyncService schuleExternalSyncService;
    private final KlasseExternalRepo klasseExternalRepo;
    private final KlasseExternalSyncService klasseExternalSyncService;
    private final KindExternalRepo kindExternalRepo;
    private final KindExternalSyncService kindExternalSyncService;
    private final PupilMailService pupilMailService;
    private final TenantProducer tenantProducer;
    private final LehrpersonExternalRepo lehrpersonExternalRepo;
    private final LehrpersonExternalSyncService lehrpersonExternalSyncService;
    private final KontaktPersonKindExternalRepo kontaktPersonKindExternalRepo;
    private final KontaktPersonKindExternalSyncService kontaktPersonKindExternalSyncService;

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public PupilImportResultJson processPupilCsvUpload(@NonNull FileUpload fileUpload) {
        // check the filetype
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.PUPIL_SYNC);
        // store the sync data in a file
        PupilSyncEvent pupilSyncEvent = this.savePupilSyncData(fileUpload);
        InputStream is = createInputStreamFromPath(fileUpload.filePath());
        return handlePupilCSVUpload(is, pupilSyncEvent);
    }

    @ActivateRequestContext
    @ConsumeEvent(value = PUPIL_SYNC_EVENT_ADDRESS, blocking = true)
    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void handlePupilSyncEvent(@NonNull PupilSyncEvent pupilSyncEvent) {
        try {
            tenantProducer.setCurrentTenant(TenantEnum.WIN); // Pupil Import ist fix für WIN
            var bytes = fileStoreService.getFileBytesSync(pupilSyncEvent.getFileData()
                .getFileInfoBucket()
                .getFileIdInBucket());
            if (bytes == null) {
                String msg = "Pupil: File not found in S3 bucket: " + pupilSyncEvent.getFileData()
                    .getFileInfoBucket()
                    .getFileIdInBucket();
                LOG.error(msg);
               pupilMailService.sendErrorMail(msg);
                return;
            }
            this.handlePupilCSVUpload(new ByteArrayInputStream(bytes), pupilSyncEvent);
        } catch (Exception e) {
            String msg = ExceptionUtils.getStackTrace(e);
            pupilMailService.sendErrorMail(msg);
        }
    }


    @NonNull
    protected PupilSyncEvent savePupilSyncData(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfo(fileUpload, FileTyp.PUPIL_SYNC);
        // upload the file to S3
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileUpload, fileInfo);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Uploaded pupil csv file  to S3: {}", putObjectResponse);

        LocalDate today = appClock.getToday();
        final String originalFileName = fileInfo.getFileName().getFileName();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            List.of(),
            today,
            "Pupil-Sync",
            FileTyp.PUPIL_SYNC,
            originalFileName);

        FileData fileData = new FileData(FileTyp.PUPIL_SYNC, calculatedFileName, fileInfo);

        PupilSyncEvent event = new PupilSyncEvent(fileData);
        this.pupilSyncEventRepo.persist(event);
        return event;
    }

    @NonNull
    private ImportPlanDTO<LehrpersonExternal> extractLehrpersonenAndDetermineUpdatePlan(
        @NonNull List<PupilCsvDTO> escadaCsvDTOS
    ) {
        Set<LehrpersonExternal> importedItems = extractUniqueExternalLehrpersonen(escadaCsvDTOS);
        LOG.debug("Extracted {} unique Lehrpersonen schule from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(lehrpersonExternalRepo.findAllExternalIds());
        ImportPlanDTO<LehrpersonExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "PUPIL: Detected {} new Lehrpersonen schule, {} deleted and {} to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private Set<LehrpersonExternal> extractUniqueExternalLehrpersonen(
        @NonNull List<PupilCsvDTO> pupilCsvDTOs
    ) {
        // Liste aller KP erstellen (die im CSV flach als Lehrperson 1 und 2 vorliegen)
        List<LehrpersonExternal> list = pupilCsvDTOs.stream()
            // For each DTO, generate the persons in order
            .flatMap(dto -> Stream.of(
                    PupilCSVMapperService.toExternalLehrperson1(dto),
                    PupilCSVMapperService.toExternalLehrperson2(dto)
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
    private PupilImportResultJson handlePupilCSVUpload(
        @NonNull InputStream inputStream,
        @NonNull PupilSyncEvent pupilSyncEvent
    ) {
        StopWatch stopWatch = StopWatch.createStarted();

        // Create a reader for the csv file and parse the content into a list of DTOs
        Reader reader = createReaderFromInputStream(inputStream);
        List<PupilCsvDTO> pupilCsvDTOS = this.parsePupilCsvAndRemoveUndesired(reader);

        // handle schulen
        var schuleResultItems = extractAndSyncSchulen(pupilCsvDTOS);

        // handle klassen
        var klassenResultItems = extractAndSyncKlassen(pupilCsvDTOS);

        // handle lehrpersonen
        var lehrpersonenResultItems = extractAndSyncLehrpersonen(pupilCsvDTOS);

        // handle Kinder
        var kinderResultItems = extractAndSyncKinder(pupilCsvDTOS);

        // handle kontaktpersonen kinder
        var kontaktPersonKindResultItems = extractAndSyncKontaktPersonKind(pupilCsvDTOS);

        var resultReport = new PupilImportResultJson(
            pupilSyncEvent.getId(),
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
    public List<PupilCsvDTO> parsePupilCsvAndRemoveUndesired(@NonNull Reader reader) {
        return pupilCsvService.parsePupilCSV(reader);
    }

    @NonNull
    List<ItemSyncResultJson<Schule>> extractAndSyncSchulen(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        ImportPlanDTO<SchuleExternal> importPlanDTO = extractSchulenAndDetermineUpdatePlan(pupilCsvDTOS);
        return schuleExternalSyncService.syncSchulen(importPlanDTO, ImportTyp.PUPIL);
    }

    @NonNull
    List<ItemSyncResultJson<Klasse>> extractAndSyncKlassen(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        ImportPlanDTO<KlasseExternal> importPlanDTO = extractKlassenAndDetermineUpdatePlan(pupilCsvDTOS);
        return klasseExternalSyncService.syncKlassen(importPlanDTO, ImportTyp.PUPIL);
    }

    @NonNull
    List<ItemSyncResultJson<Lehrperson>> extractAndSyncLehrpersonen(@NonNull List<PupilCsvDTO> pupilCsvDTOs) {
        ImportPlanDTO<LehrpersonExternal> importPlanDTO = extractLehrpersonenAndDetermineUpdatePlan(pupilCsvDTOs);
        return lehrpersonExternalSyncService.syncLehrpersonen(importPlanDTO, ImportTyp.PUPIL);
    }

    @NonNull
    List<ItemSyncResultJson<Kind>> extractAndSyncKinder(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        ImportPlanDTO<KindExternal> importPlanDTO = extractKinderAndDetermineUpdatePlan(pupilCsvDTOS);
        return kindExternalSyncService.syncKinder(importPlanDTO, ImportTyp.PUPIL);
    }

    @NonNull
    List<ItemSyncResultJson<KontaktPersonKind>> extractAndSyncKontaktPersonKind(@NonNull List<PupilCsvDTO> pupilCsvDTOs) {
        ImportPlanDTO<KontaktPersonKindExternal> importPlanDTO = extractKontaktPersonenKindAndDetermineUpdatePlan(pupilCsvDTOs);
        return kontaktPersonKindExternalSyncService.syncKontaktPersonKind(importPlanDTO, ImportTyp.PUPIL);
    }

    @NonNull
    private ImportPlanDTO<SchuleExternal> extractSchulenAndDetermineUpdatePlan(
        @NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        Set<SchuleExternal> importedItems = extractUniqueExternalSchulen(pupilCsvDTOS);
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
    private Set<SchuleExternal> extractUniqueExternalSchulen(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        return pupilCsvDTOS.stream()
            .map(PupilCSVMapperService::toExternalSchule)
            .flatMap(Optional::stream) // unwrap the optional and only continue with the present values
            .collect(toUniqueExternalIdSet());
    }

    @NonNull
    private ImportPlanDTO<KlasseExternal> extractKlassenAndDetermineUpdatePlan(
        @NonNull List<PupilCsvDTO> pupilCsvDTOS
    ) {
        Set<KlasseExternal> importedItems = extractUniqueExternalKlassen(pupilCsvDTOS);
        LOG.debug("Extracted {} unique klassen from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(klasseExternalRepo.findAllExternalIds());
        ImportPlanDTO<KlasseExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "PUPIL: Detected {} new klassen, {} deleted klassen and {} klassen to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private Set<KlasseExternal> extractUniqueExternalKlassen(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {
        return pupilCsvDTOS.stream()
            .map(PupilCSVMapperService::toExternalKlasse)
            .flatMap(Optional::stream)
            .collect(toUniqueExternalIdSet());
    }

    @NonNull
    private ImportPlanDTO<KindExternal> extractKinderAndDetermineUpdatePlan(
        @NonNull List<PupilCsvDTO> pupilCsvDTOS
    ) {
        Set<KindExternal> importedItems = extractUniqueExternalKinder(pupilCsvDTOS);
        LOG.debug("Extracted {} unique kinder from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(kindExternalRepo.findAllExternalIds());
        ImportPlanDTO<KindExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "PUPIL: Detected {} new kinder, {} deleted kinder and {} kinder to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    Set<KindExternal> extractUniqueExternalKinder(@NonNull List<PupilCsvDTO> pupilCsvDTOS) {

        Map<String, List<KindExternal>> kindIdsToEntries = pupilCsvDTOS.stream()
            .map(PupilCSVMapperService::toExternalKind)
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
                    "PUPIL Found multiple entries for kind with externalId: '{}', keeping one at random",
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
    private ImportPlanDTO<KontaktPersonKindExternal> extractKontaktPersonenKindAndDetermineUpdatePlan(
        @NonNull List<PupilCsvDTO> pupilCsvDTOS
    ) {
        Set<KontaktPersonKindExternal> importedItems = extractUniqueExternalKontaktPersonenKind(pupilCsvDTOS);
        LOG.debug("Extracted {} unique Lehrpersonen kinder from the csv", importedItems.size());

        Set<ExternalSystemIdDTO> existingItems = new HashSet<>(kontaktPersonKindExternalRepo.findAllExternalIds());
        ImportPlanDTO<KontaktPersonKindExternal> result = ImportPlanDTO.from(existingItems, importedItems);

        LOG.info(
            "PUPIL: Detected {} new Lehrpersonen kinder, {} deleted and {} to sync",
            result.itemsToAdd().size(),
            result.itemsToDelete().size(),
            result.itemIdsToSync().size());
        return result;
    }

    @NonNull
    private Set<KontaktPersonKindExternal> extractUniqueExternalKontaktPersonenKind(
        @NonNull List<PupilCsvDTO> pupilCsvDTOS
    ) {
        // Liste aller KP erstellen (die im CSV flach als KP 1-2 vorliegen), i.e. 'unflatten'
        List<KontaktPersonKindExternal> list = pupilCsvDTOS.stream()
            // For each DTO, generate the persons in order
            .flatMap(dto -> Stream.of(
                    PupilCSVMapperService.toExternalKontaktPersonKind1(dto),
                    PupilCSVMapperService.toExternalKontaktPersonKind2(dto)
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

    private void reportSummaryByMail(@NonNull PupilImportResultJson resultReport) {
        if (resultReport.hasErrors() && configService.isPupilSyncErrorMailEnabled()) {
            // send mail
            LOG.error("PUPIL-CSV-Import FINISHED with errors: Sending error mail for the sync");
            pupilMailService.sendWarningMail(resultReport);
        } else {
            LOG.info("PUPIL-CSV-Import SUCCESSFUL");
        }
    }

}
