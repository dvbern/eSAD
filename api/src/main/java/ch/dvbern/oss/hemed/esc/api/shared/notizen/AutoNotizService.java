package ch.dvbern.oss.hemed.esc.api.shared.notizen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
        moveNotiz(
            einladungSource,
            einladungTarget,
            Einladung::getImpfausweisNotiz,
            "einladung.impfausweis.notiz.title",
            "einladung.impfausweis.notiz.bemerkung.nachzuegler"
        );
    }

    public void moveFragebogenNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        moveNotiz(
            einladungSource,
            einladungTarget,
            Einladung::getFragebogenNotiz,
            "einladung.fragebogen.notiz.title",
            "einladung.fragebogen.notiz.bemerkung.nachzuegler"
        );
    }

    public void moveAnmeldungBestaetigungNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        moveNotiz(
            einladungSource,
            einladungTarget,
            Einladung::getAnmeldungBestaetigungNotiz,
            "einladung.anmeldungBestaetigung.notiz.title",
            "einladung.anmeldungBestaetigung.notiz.bemerkung.nachzuegler"
        );
    }

    public void moveImpfeinverstaendnisNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget
    ) {
        moveNotiz(
            einladungSource,
            einladungTarget,
            Einladung::getImpfeinverstaendnisNotiz,
            "einladung.impfeinverstaendnis.notiz.title",
            "einladung.impfeinverstaendnis.notiz.bemerkung.nachzuegler"
        );
    }

    private void moveNotiz(
        @NonNull Einladung einladungSource,
        @NonNull Einladung einladungTarget,
        @NonNull Function<Einladung, Optional<Notiz>> notizExtractor,
        @NonNull String titleTranslationKey,
        @NonNull String bemerkungTranslationKey
    ) {
        Kind kind = einladungTarget.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditEntityForInactiveKind(
                kind,
                new Notiz()
            ));

        Notiz existingNotiz = notizExtractor.apply(einladungSource).orElse(null);
        if (existingNotiz != null) {
            String newTitle = tl.translate(
                titleTranslationKey,
                PARAM_ANLASSNAME, einladungTarget.getAnlass().getFullAnlassTitel(tl));

            // Add a note to the existing bemerkungen
            StringBuilder sb = new StringBuilder();
            String existingBemerkungen = existingNotiz.getBemerkungen();
            if (existingBemerkungen != null) {
                sb.append(existingBemerkungen).append('\n');
            }
            sb.append(tl.translate(
                bemerkungTranslationKey,
                PARAM_ANLASSNAME, einladungSource.getAnlass().getFullAnlassTitel(tl)));
            String newBemerkungen = sb.toString();

            Notiz copyOfNotiz = Notiz.copyFromExisting(existingNotiz, newTitle, newBemerkungen);

            einladungSource.getNotizen().remove(existingNotiz);
            notizService.remove(existingNotiz.getId(), false);

            List<Notiz> notizen = new ArrayList<>(einladungTarget.getNotizen().stream().toList());
            Notiz notiz = notizService.persist(copyOfNotiz);
            notizen.add(notiz);
            einladungTarget.setNotizen(notizen);
            LOG.info("Moved Notiz from einladung {} to einladung {}", einladungSource.getId(), einladungTarget.getId());
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
            case NotizTyp.IMPFEINVERSTAENDNIS -> Label.ID_LABEL_IMPFEINVERSTAENDNIS;
            case NotizTyp.DEFAULT -> null;
        };
    }
}
