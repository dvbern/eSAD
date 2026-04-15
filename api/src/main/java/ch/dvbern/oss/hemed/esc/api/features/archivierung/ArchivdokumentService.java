package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivdokumentService {

    private final ArchivdokumentRepo archivdokumentRepo;
    private final ArchivdokumentMailService archivdokumentMailService;
    private final CurrentUserService currentUserService;
    private final AppClock appClock;

    @NonNull
    public Archivdokument persist(@NonNull Archivdokument dokument) {
        archivdokumentRepo.persist(dokument);
        return dokument;
    }

    @NonNull
    public List<Archivdokument> findAll() {
        return archivdokumentRepo.findAll();
    }

    @NonNull
    public List<Archivdokument> findAll(boolean onlyActive) {
        List<ArchivierungStatus> archivierungStatusList = ArchivierungStatus.offeneStatus();
        if (!onlyActive) {
            archivierungStatusList = ArchivierungStatus.abgeschlosseneStatus();
        }

        Benutzer currentUser = currentUserService.currentUser();
        if (currentUser.getFachRollen().contains(FachRolle.SUPPORT)) {
            return archivdokumentRepo.findAll(archivierungStatusList);
        }

        return archivdokumentRepo.findAllBenutzer(currentUser.getId(), archivierungStatusList);
    }

    @NonNull
    public Archivdokument getById(@NonNull ID<Archivdokument> archivdokumentID) {
        return archivdokumentRepo.getById(archivdokumentID);
    }

    public void sendArchiveDocumentDownloadReminder() {
        List<Benutzer> benutzerWithOffeneDokumente =
            archivdokumentRepo.findArchivOwnersHavingDocumentsInStatus(ArchivierungStatus.OFFEN);
        archivdokumentMailService.sendArchiveDocumentDownloadReminder(benutzerWithOffeneDokumente);
    }

    @NonNull
    public List<ID<Dossier>> findKinderAelterAlsUndNichtArchiviert(int minAge, int batchsize) {
        return archivdokumentRepo.findKinderAelterAlsUndNichtArchiviert(minAge, batchsize);
    }

    public void setArchivierungStatusAndTimestamp(@NonNull Archivdokument archivdokument) {
        if (archivdokument.getArchivierungStatus() == ArchivierungStatus.OFFEN) {
            archivdokument.setTimestampDownloaded(appClock.offsetDateTime().now());
            if (archivdokument.getArchivierungTyp() == ArchivierungTyp.KIND_DEAKTIVIERT) {
                archivdokument.setArchivierungStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
            } else {
                archivdokument.setArchivierungStatus(ArchivierungStatus.DOWNLOADED);
            }
            persist(archivdokument);
        }
    }

    public List<ID<Dossier>> findKinderReadyToDelete(int monthsBeforeDeletion, int batchsize) {
        return archivdokumentRepo.findKinderThatAreReadyToDelete(monthsBeforeDeletion, batchsize);
    }

    public void removeForDossier(@NonNull ID<Dossier> dossierID) {
        List<Archivdokument> archivdokumentList = archivdokumentRepo.findAllForDossier(dossierID);
        for (Archivdokument archivdokument : archivdokumentList) {
            archivdokumentRepo.remove(archivdokument.getId());
        }
    }
}
