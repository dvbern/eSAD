package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.nio.file.Path;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public class ArchivdokumentMapper {

    private final ArchivdokumentService archivdokumentService;
    private final TenantContext tenantContext;

    @NonNull
    public static ArchivdokumentJson toJson(@NonNull Archivdokument archivdokument, @NonNull TenantEnum tenant) {
        return new ArchivdokumentJson(
            archivdokument.getId(),
            archivdokument.getTimestampArchiviert(),
            archivdokument.getArchivierungTyp(),
            archivdokument.getArchivierungStatus(),
            archivdokument.getBetroffenesKind(),
            FileDataMapper.toJson(archivdokument.getFiledata()),
            BenutzerMapper.toOptionJson(archivdokument.getOwner(), tenant),
            archivdokument.getTimestampDownloaded()
        );
    }

    @NonNull
    public List<ArchivdokumentJson> findAll(boolean onlyOpen) {
        TenantEnum tenant = tenantContext.get();
        return archivdokumentService.findAll(onlyOpen).stream().map(a -> ArchivdokumentMapper.toJson(a, tenant)).toList();
    }


    @NonNull
    public FileInfo downloadArchivFile(@NonNull ID<Archivdokument> archivdokumentId) {
        return archivdokumentService.downloadArchivFile(archivdokumentId);
    }

    @NonNull
    public Path bulkDownloadArchivFilesAsZip(@NonNull List<ID<Archivdokument>> archivdokumentIds) {
        return archivdokumentService.downloadArchivFilesAsZip(archivdokumentIds);
    }
}
