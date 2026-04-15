package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public class ArchivdokumentMapper {

    private final ArchivdokumentService archivdokumentService;

    @NonNull
    public static ArchivdokumentJson toJson(@NonNull Archivdokument archivdokument) {
        return new ArchivdokumentJson(
            archivdokument.getId(),
            archivdokument.getTimestampArchiviert(),
            archivdokument.getArchivierungTyp(),
            archivdokument.getArchivierungStatus(),
            archivdokument.getBetroffenesKind(),
            FileDataMapper.toJson(archivdokument.getFiledata()),
            BenutzerMapper.toOptionJson(archivdokument.getOwner()),
            archivdokument.getTimestampDownloaded()
        );
    }

    @NonNull
    public List<ArchivdokumentJson> findAll(boolean onlyOpen) {
        return archivdokumentService.findAll(onlyOpen).stream().map(ArchivdokumentMapper::toJson).toList();
    }

    @NonNull
    public FileInfo downloadArchivFile(@NonNull ID<Archivdokument> archivdokumentId) {
        Archivdokument archivdokument = archivdokumentService.getById(archivdokumentId);
        if (archivdokument.getFiledata() == null) {
            throw new AppFailureException(AppFailureMessage.entityNotFound(archivdokumentId, "File not found"));
        }
        archivdokumentService.setArchivierungStatusAndTimestamp(archivdokument);
        return archivdokument.getFiledata().getFileInfoBucket();
    }
}
