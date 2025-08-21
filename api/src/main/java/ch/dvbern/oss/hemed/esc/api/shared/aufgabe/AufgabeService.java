package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3Mapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AufgabeService {

    private final AufgabeRepo aufgabeRepo;

    private final FileStoreService fileStoreService;
    private final FileValidationService fileValidationService;
    private final ConfigService configService;
    private final CurrentUserService currentUserService;
    private final AppClock appClock;

    @NonNull
    public Aufgabe persist(@NonNull Aufgabe aufgabe) {
        aufgabeRepo.persist(aufgabe);
        return aufgabe;
    }

    public void remove(@NonNull ID<Aufgabe> aufgabeID) {
        aufgabeRepo.remove(aufgabeID);
    }

    public void removeForKind(@NonNull ID<Kind> kindID) {
        List<Aufgabe> aufgaben = findAllForKind(kindID);
        aufgaben.stream().map(AbstractEntity::getId).forEach(this::remove);
    }

    @NonNull
    public List<Aufgabe> findAll() {
        return aufgabeRepo.findAll();
    }

    @NonNull
    public List<Aufgabe> findAllForKind(@NonNull ID<Kind> kindId) {
        return aufgabeRepo.findAllForKind(kindId);
    }

    @NonNull
    public List<Aufgabe> findAufgabenForAnlassAndLabel(@NonNull Anlass anlass, @NonNull ID<Label> labelId) {
        return aufgabeRepo.findAufgabenForAnlassAndLabel(anlass, labelId);
    }

    @NonNull
    public List<Aufgabe> findAufgabenForKindWithLabel(@NonNull Kind kind, @NonNull ID<Label> labelId) {
        return aufgabeRepo.findAufgabenForKindWithLabel(kind, labelId);
    }

    @NonNull
    public Optional<Aufgabe> findSingleAufgabeForKindWithLabel(@NonNull Kind kind, @NonNull ID<Label> labelId) {
        List<Aufgabe> existing = aufgabeRepo.findAufgabenForKindWithLabel(kind, labelId);
        if (existing.size() > 1) {
            throw new IllegalStateException("Es gibt mehr als eine Aufgabe mit Label " + labelId + " für das Kind " + kind.getId());
        }
        return existing.stream().findFirst();
    }

    @NonNull
    public Aufgabe getById(@NonNull ID<Aufgabe> aufgabeID) {
        return aufgabeRepo.getById(aufgabeID);
    }

    @NonNull
    public Aufgabe addUploadedFile(@NonNull ID<Aufgabe> aufgabeID, @NonNull FileUpload file) {
        fileValidationService.requireFiletypeIsAllowed(file, FileTyp.AUFGABEUPLOAD);
        FileInfoS3 fileInfoS3 = saveAufgabeAttachmentInFilestorage(file);
        Aufgabe aufgabe = aufgabeRepo.getById(aufgabeID);
        FileTyp fileTyp = FileNameUtil.getTypFromLabel(aufgabe.getLabels(), FileTyp.AUFGABEUPLOAD);
        String nameVorname = aufgabe.getKind() != null ? aufgabe.getKind().getNameVorname() : "Aufgabe";
        LocalDate today = appClock.localDateTime().now().toLocalDate();
        final String originalFileName = fileInfoS3.getFileName().getFileName();
        String calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            aufgabe.getFiles(),
            today,
            nameVorname,
            fileTyp, originalFileName);

        FileData fileData = new FileData(fileTyp, calculatedFileName, fileInfoS3);
        return aufgabeRepo.addUploadedFile(aufgabeID, fileData);
    }

    public void removeUploadedFile(@NonNull Aufgabe loadedAufgabe, @NonNull ID<FileData> fileId) {
        FileInfoS3 fileToRemove = findUploadedFileInfoS3(loadedAufgabe.getId(), fileId);
        boolean removedFromList = loadedAufgabe.getFiles().removeIf(fileData -> fileId.equals(fileData.getId()));
        if (!removedFromList) {
            throw new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found"));
        }

        Uni<DeleteObjectResponse> deleteObjectResponseUni =
            fileStoreService.deleteInS3Async(fileToRemove.getFileIdInBucket());

        DeleteObjectResponse deleteResponse =
            deleteObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.info("Deleted file from S3: {}", deleteResponse);
    }

    @NonNull
    private FileInfoS3 saveAufgabeAttachmentInFilestorage(@NonNull FileUpload fileUpload) {
        // prepare the file info Embeddable
        FileInfoS3 fileInfoS3 = FileInfoS3Mapper.mapFileUploadToFileInfoS3(fileUpload, FileTyp.AUFGABEUPLOAD);
        // upload the file to S3
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileUpload, fileInfoS3);
        PutObjectResponse putObjectResponse = putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        LOG.debug("Uploaded file to S3: {}", putObjectResponse);

        return fileInfoS3;
    }

    @NonNull
    public FileInfoS3 findUploadedFileInfoS3(@NonNull ID<Aufgabe> aufgabeId, @NonNull ID<FileData> fileId) {
        return getById(aufgabeId).getFiles().stream()
            .filter(fileData -> fileData.getId().equals(fileId))
            .findFirst()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(fileId, "File not found")))
            .getFileInfoBucket();
    }

    public int getNumOfOffeneFaelligeAufgaben() {
        return aufgabeRepo.getNumOfOffeneFaelligeAufgaben();
    }

    public int getNumOfOffeneFaelligeAufgabenByCurrentUser() {
        ID<Benutzer> currentUserId = currentUserService.currentUserRef().getId();
        return aufgabeRepo.getOffeneFaelligeAufgabenByEmpfaenger(currentUserId)
            .size();
    }

    @NonNull
    public List<Aufgabe> findAllNichtAbgeschlosseneAufgabenForCurrent() {
        ID<Benutzer> currentUserId = currentUserService.currentUserRef().getId();
        return aufgabeRepo.findAllNichtAbgeschlosseneAufgabenForCurrent(currentUserId);
    }
}
