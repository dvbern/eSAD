package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungStatus;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungTyp;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ArchivdokumentBuilder {

    private final Archivdokument archivdokument;

    private ArchivdokumentBuilder() {
        this.archivdokument = defaultArchivdokument();
        TestUtil.setEntityInfo(archivdokument);
    }

    @NonNull
    public static Archivdokument defaultArchivdokument() {
        return new Archivdokument(
            OffsetDateTime.now(),
            ArchivierungTyp.KIND_DEAKTIVIERT,
            ArchivierungStatus.OFFEN,
            UUID.randomUUID(),
            "Test-Kind (01.01.1980)",
            FileDataBuilder.defaultPdfFileData(),
            BenutzerBuilder.defaultBenutzer(),
            null
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withArchivierungTyp(@NonNull ArchivierungTyp typ) {
        this.archivdokument.setArchivierungTyp(typ);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withDossierID(@NonNull UUID dossierID) {
        this.archivdokument.setDossierID(dossierID);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withOwner(@NonNull Benutzer owner) {
        this.archivdokument.setOwner(owner);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withFiledata(@NonNull FileData fileData) {
        this.archivdokument.setFiledata(fileData);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withStatus(@NonNull ArchivierungStatus status) {
        this.archivdokument.setArchivierungStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public ArchivdokumentBuilder withTimestampDownloaded(@NonNull OffsetDateTime timestampDownloaded) {
        this.archivdokument.setTimestampDownloaded(timestampDownloaded);
        return this;
    }

    @NonNull
    public static Archivdokument create(@NonNull Consumer<ArchivdokumentBuilder> block) {
        ArchivdokumentBuilder builder = new ArchivdokumentBuilder();
        block.accept(builder);
        return builder.archivdokument;
    }
}
