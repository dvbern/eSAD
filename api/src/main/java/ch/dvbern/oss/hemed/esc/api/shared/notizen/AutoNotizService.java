package ch.dvbern.oss.hemed.esc.api.shared.notizen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoNotizService {

    public static final String PARAM_ANLASSNAME = "anlassname";

    private final NotizService notizService;
    private final LabelService labelService;
    private final CurrentUserService currentUserService;
    private final TL tl;
    private final FileDataService fileDataService;

    @NonNull
    public Notiz createDuplikatMergeNotiz(
        @NonNull Dossier dossier,
        @NonNull PrintResultDTO exportBasisKind,
        @NonNull PrintResultDTO exportOtherKind
    ) {
        KindValidationUtil.throwErrorIfKindIsInactive(
            dossier.getKind(),
            AppValidationMessage.cannotCreateEntityForInactiveKind(
                dossier.getKind(),
                new Notiz()
            ));
        Notiz notiz = new Notiz(
            tl.translate("notiz.duplikat.merge.titel"),
            null,
            NotizStatus.ABGESCHLOSSEN,
            NotizTyp.DEFAULT,
            dossier,
            currentUserService.currentUser(),
            null,
            Collections.emptySet(),
            List.of(
                fileDataService.createFileData(exportBasisKind, FileTyp.KINDERDOSSIER, tl.translate("notiz.duplikat.merge.file.kind1")),
                fileDataService.createFileData(exportOtherKind, FileTyp.KINDERDOSSIER, tl.translate("notiz.duplikat.merge.file.kind2"))
            )
        );
        return notizService.persist(notiz);
    }

    public void moveImpfausweisNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        Kind kind = einladungTarget.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditEntityForInactiveKind(
                kind,
                new Notiz()
            ));

        Notiz impfausweis = einladungSource.getImpfausweisNotiz().orElse(null);
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
            sb.append(tl.translate(
                "einladung.impfausweis.notiz.bemerkung.nachzuegler",
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkungen = sb.toString();

            Notiz copyOfImpfausweis = Notiz.copyFromExisting(impfausweis, newTitle, newBemerkungen);

            einladungSource.getNotizen().remove(impfausweis);
            notizService.remove(impfausweis.getId(), false);

            List<Notiz> notizen = new ArrayList<>(einladungTarget.getNotizen().stream().toList());
            Notiz notiz = notizService.persist(copyOfImpfausweis);
            notizen.add(notiz);
            einladungTarget.setNotizen(notizen);
        }
    }

    public void moveFragebogenNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        Kind kind = einladungTarget.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditEntityForInactiveKind(
                kind,
                new Notiz()
            ));
        Notiz fragebogen = einladungSource.getFragebogenNotiz().orElse(null);

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
            sb.append(tl.translate(
                "einladung.fragebogen.notiz.bemerkung.nachzuegler",
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkung = sb.toString();

            Notiz copyOfFragebogen = Notiz.copyFromExisting(fragebogen, newTitle, newBemerkung);

            einladungSource.getNotizen().remove(fragebogen);
            notizService.remove(fragebogen.getId(), false);

            List<Notiz> notizen = new ArrayList<>(einladungTarget.getNotizen().stream().toList());
            Notiz notiz = notizService.persist(copyOfFragebogen);
            notizen.add(notiz);
            einladungTarget.setNotizen(notizen);
        }
    }

    public void moveAnmeldungBestaetigungNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        Kind kind = einladungTarget.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditEntityForInactiveKind(
                kind,
                new Notiz()
            ));
        Notiz anmeldungBestaetigung = einladungSource.getAnmeldungBestaetigungNotiz().orElse(null);

        if (anmeldungBestaetigung != null) {
            String newTitle = tl.translate(
                "einladung.anmeldungBestaetigung.notiz.title",
                PARAM_ANLASSNAME, einladungTarget.getAnlass().getFullAnlassTitel(tl));

            // Add a note to the existing bemerkungen
            StringBuilder sb = new StringBuilder();
            String existingBemerkungen = anmeldungBestaetigung.getBemerkungen();
            if (existingBemerkungen != null) {
                sb.append(existingBemerkungen).append('\n');
            }
            sb.append(tl.translate(
                "einladung.anmeldungBestaetigung.notiz.bemerkung.nachzuegler",
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkung = sb.toString();

            Notiz copyOfAnmeldungBestaetigung = Notiz.copyFromExisting(anmeldungBestaetigung, newTitle, newBemerkung);

            einladungSource.getNotizen().remove(anmeldungBestaetigung);
            notizService.remove(anmeldungBestaetigung.getId(), false);

            List<Notiz> notizen = new ArrayList<>(einladungTarget.getNotizen().stream().toList());
            Notiz notiz = notizService.persist(copyOfAnmeldungBestaetigung);
            notizen.add(notiz);
            einladungTarget.setNotizen(notizen);
        }
    }

    @NonNull
    public Notiz createNotizWithLabel(
        @NonNull Notiz notiz,
        @Nullable ID<Label> labelId
    ) {
        Set<Label> desiredLabels = new LinkedHashSet<>(notiz.getLabels());
        if (labelId != null) {
            Label label = labelService.getById(labelId);
            desiredLabels.add(label);
        }
        notiz.setLabels(desiredLabels);
        notiz.setStatus(NotizStatus.ABGESCHLOSSEN);
        return notizService.persist(notiz);
    }

    @NonNull
    public Notiz createEinladungNotiz(
        @NonNull Einladung einladung,
        @NonNull Notiz notiz,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz savedNotiz = this.createNotizWithLabel(notiz, this.getAutoLabelId(notiz.getTyp()));
        savedNotiz.disableOptimisticLockingCheck();
        for (FileUpload fileUpload : fileUploads) {
            savedNotiz = notizService.addUploadedFileAndFlush(savedNotiz.getId(), fileUpload);
        }
        einladung.getNotizen().add(savedNotiz);
        return savedNotiz;
    }

    @Nullable
    private ID<Label> getAutoLabelId(@NonNull NotizTyp notizTyp) {
        return switch (notizTyp) {
            case NotizTyp.FRAGEBOGEN -> Label.ID_LABEL_FRAGEBOGEN;
            case NotizTyp.ANMELDUNG_BESTAETIGUNG -> Label.ID_LABEL_ANMELDUNG_BESTAETIGUNG;
            case NotizTyp.IMPFAUSWEIS -> Label.ID_LABEL_IMPFAUSWEIS;
            case NotizTyp.ABMELDUNG -> Label.ID_LABEL_ABMELDUNGSGRUND;
            case NotizTyp.DEFAULT -> null;
        };
    }
}
