package ch.dvbern.oss.hemed.esc.api.shared.notizen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoNotizService {

    private static final String PARAM_ANLASSNAME = "anlassname";

    private final NotizService notizService;
    private final LabelService labelService;
    private final CurrentUserService currentUserService;
    private final FileStoreService fileStoreService;
    private final ConfigService configService;
    private final TL tl;

    @NonNull
    public Notiz createDuplikatMergeNotiz(
        @NonNull Dossier dossier,
        @NonNull PrintResultDTO exportBasisKind,
        @NonNull PrintResultDTO exportOtherKind
    ) {
        Notiz notiz = new Notiz(
            tl.translate("notiz.duplikat.merge.titel"),
            null,
            NotizStatus.ABGESCHLOSSEN,
            dossier,
            currentUserService.currentUser(),
            Collections.emptySet(),
            List.of(
                createFileData(exportBasisKind, tl.translate("notiz.duplikat.merge.file.kind1")),
                createFileData(exportOtherKind, tl.translate("notiz.duplikat.merge.file.kind2"))
            )
        );
        return notizService.persist(notiz);
    }

    public void moveImpfausweisNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        Notiz impfausweis = einladungSource.getImpfausweis();
        if (impfausweis != null) {
            String newTitle = tl.translate(
                "einladung.impfausweis.notiz.title",
                PARAM_ANLASSNAME, einladungTarget.getAnlass().getFullAnlassTitel(tl));

            // Add a note to the existing bemerkungen
            StringBuilder sb = new StringBuilder();
            String existingBemerkungen = impfausweis.getBemerkungen();
            if (existingBemerkungen != null) {
                sb.append(existingBemerkungen).append('\n');
            }
            sb.append(tl.translate("einladung.impfausweis.notiz.bemerkung.nachzuegler",
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkungen = sb.toString();

            Notiz copyOfImpfausweis = Notiz.copyFromExisting(impfausweis, newTitle, newBemerkungen);

            einladungSource.setImpfausweis(null);
            notizService.remove(impfausweis.getId());
            einladungTarget.setImpfausweis(notizService.persist(copyOfImpfausweis));

        }
    }

    public void moveFragebogenNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        Notiz fragebogen = einladungSource.getFragebogen();
        if (fragebogen != null) {
            String newTitle = tl.translate(
                "einladung.fragebogen.notiz.title",
                PARAM_ANLASSNAME, einladungTarget.getAnlass().getFullAnlassTitel(tl));

            // Add a note to the existing bemerkungen
            StringBuilder sb = new StringBuilder();
            String existingBemerkungen = fragebogen.getBemerkungen();
            if (existingBemerkungen != null) {
                sb.append(existingBemerkungen).append('\n');
            }
            sb.append(tl.translate("einladung.fragebogen.notiz.bemerkung.nachzuegler",
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkung = sb.toString();

            Notiz copyOfFragebogen = Notiz.copyFromExisting(fragebogen, newTitle, newBemerkung);

            einladungSource.setFragebogen(null);
            notizService.remove(fragebogen.getId());
            einladungTarget.setFragebogen(notizService.persist(copyOfFragebogen));

        }
    }

    @NonNull
    private FileData createFileData(@NonNull PrintResultDTO exportKind, @NonNull String prefixFilename) {
        // automatisch generierter Name mit Prfefix ergänzen damit man sieht dass das Dok von Merge ist
        CleanFileName cleanFileName = new CleanFileName(prefixFilename + '_' + exportKind.getFileInfo().getFileName());
        FileInfoS3 fileInfoS3 = FileInfoS3.of(exportKind.getFileInfo());
        fileInfoS3.setFileName(cleanFileName);
        Uni<PutObjectResponse> putObjectResponseUni = fileStoreService.uploadToS3Async(fileInfoS3.getMediaType().toString(),exportKind.getContent(), fileInfoS3);
        putObjectResponseUni.await().atMost(configService.getS3UploadTimeout());
        return new FileData(FileTyp.KINDERDOSSIER, fileInfoS3.getFileName().toString(), fileInfoS3);
    }

    @NonNull
    public Notiz createNotizWithLabel(
        @NonNull Notiz notiz,
        @NonNull ID<Label> labelId
    ) {
        Label label = labelService.getById(labelId);
        Set<Label> desiredLabels = new LinkedHashSet<>(notiz.getLabels());
        desiredLabels.add(label);
        notiz.setLabels(desiredLabels);
        notiz.setStatus(NotizStatus.ABGESCHLOSSEN);
        return notizService.persist(notiz);
    }

    @NonNull
    public Notiz createImpfausweisNotiz(
        @NonNull Einladung einladung,
        @NonNull Notiz notiz,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz savedNotiz = this.createNotizWithLabel(notiz, Label.ID_LABEL_IMPFAUSWEIS);
        for (FileUpload fileUpload : fileUploads) {
            savedNotiz = notizService.addUploadedFile(savedNotiz.getId(), fileUpload);
        }
        einladung.setImpfausweis(savedNotiz);
        return savedNotiz;
    }

    @NonNull
    public Notiz createFragebogenNotiz(
        @NonNull Einladung einladung,
        @NonNull Notiz notiz,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz savedNotiz = this.createNotizWithLabel(notiz, Label.ID_LABEL_FRAGEBOGEN);
        for (FileUpload fileUpload : fileUploads) {
            savedNotiz = notizService.addUploadedFile(savedNotiz.getId(), fileUpload);
        }
        einladung.setFragebogen(savedNotiz);
        return savedNotiz;
    }
}
