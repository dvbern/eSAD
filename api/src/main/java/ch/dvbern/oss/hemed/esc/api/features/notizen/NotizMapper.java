package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindMapper;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelMapper;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@JaxMapper
@RequiredArgsConstructor
public final class NotizMapper {

    private final NotizService notizService;
    private final DossierService dossierService;
    private final BenutzerService benutzerService;
    private final LabelMapper labelMapper;
    private final AuthorizerService authorizerService;

    @NonNull
    public static NotizJson toJson(@NonNull Notiz notiz) {
        return new NotizJson(
            notiz.getId(),
            EntityInfoJson.fromEntity(notiz),
            new NotizSaveJson(
                notiz.getTitel(),
                notiz.getBemerkungen(),
                notiz.getStatus(),
                notiz.getTyp(),
                BenutzerMapper.toOptionJson(notiz.getVerantwortlicher()),
                notiz.getLabels().stream().map(LabelMapper::toJson).toList(),
                notiz.getFiles().stream().map(FileDataMapper::toJson).toList(),
                notiz.getVersion()
            ),
            KindMapper.toOptionJson(notiz.getDossier().getKind())
        );
    }

    @NonNull
    public Notiz toEntity(@NonNull Notiz existingNotiz, @NonNull NotizSaveJson saveJson) {
        existingNotiz.setTitel(saveJson.getTitel());
        existingNotiz.setBemerkungen(saveJson.getBemerkungen());
        existingNotiz.setStatus(saveJson.getStatus());
        existingNotiz.setTyp(saveJson.getTyp());
        existingNotiz.setVerantwortlicher(benutzerService.getById(saveJson.getVerantwortlicher().getId()));
        Set<Label> desiredLabels = labelMapper.toEntityList(saveJson.getLabels());
        labelMapper.requireNoDisabledOrInactivLabelAddedOrRemoved(existingNotiz.getLabels(), desiredLabels);
        existingNotiz.getLabels().clear();
        existingNotiz.getLabels().addAll(desiredLabels);
        // files will be handled separately
        return existingNotiz;
    }

    @NonNull
    public Notiz toEntityForCreate(@NonNull NotizSaveJson notizJson, @NonNull ID<Dossier> dossierId) {
        Dossier dossier = this.dossierService.getById(dossierId);
        KindValidationUtil.throwErrorIfKindIsInactive(
            dossier.getKind(),
            AppValidationMessage.cannotCreateEntityForInactiveKind(
                dossier.getKind(),
                new Notiz()));
        Benutzer verantwortlicher = this.benutzerService.getById(notizJson.getVerantwortlicher().getId());
        Set<Label> desiredLabels = labelMapper.toEntityList(notizJson.getLabels());
        return new Notiz(
            notizJson.getTitel(),
            notizJson.getBemerkungen(),
            notizJson.getStatus(),
            notizJson.getTyp(),
            dossier,
            verantwortlicher,
            desiredLabels,
            new ArrayList<>()
        );
    }

    @NonNull
    public NotizJson getById(@NonNull ID<Notiz> notizId) {
        return NotizMapper.toJson(notizService.getById(notizId));
    }

    @NonNull
    public NotizJson create(
        @NonNull NotizSaveJson notizJson,
        @NonNull ID<Dossier> dossierId,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz savedNotiz = notizService.persist(toEntityForCreate(notizJson, dossierId));
        return toJson(notizService.updateWithFiles(savedNotiz, fileUploads, notizJson.getVersion()));
    }

    @NonNull
    public NotizJson update(
        @NonNull ID<Notiz> notizId,
        @NonNull NotizSaveJson saveJson,
        @NonNull List<FileUpload> fileUploads
    ) {
        Notiz existingNotiz = notizService.getById(notizId);
        Kind kind = existingNotiz.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotEditEntityForInactiveKind(
                kind,
                existingNotiz));
        Notiz entity = toEntity(existingNotiz, saveJson);
        return toJson(notizService.updateWithFiles(entity, fileUploads, saveJson.getVersion()));
    }

    public void remove(@NonNull ID<Notiz> notizID) {
        Notiz notiz = notizService.getById(notizID);
        Kind kind = notiz.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotRemoveEntityFromInactiveKind(
                kind,
                notiz));
        notizService.remove(notizID);
    }

    @NonNull
    public NotizJson getNotiz(@NonNull ID<Notiz> notizId) {
        return toJson(notizService.getById(notizId));
    }

    @NonNull
    public List<NotizJson> findAll() {
        List<Notiz> notizenList = notizService.findAll();
        return notizenList.stream().map(NotizMapper::toJson).toList();
    }

    @NonNull
    public List<NotizJson> findAllForKind(@NonNull ID<Kind> kindID) {
        return notizService.findAllForKind(kindID).stream()
            // some Roles can only read Notizen with certain labels
            .filter(authorizerService::hasReadAuthorizationNotiz)
            .map(NotizMapper::toJson)
            .toList();
    }

    @NonNull
    public NotizJson addUploadedFile(@NonNull ID<Notiz> notizID, @NonNull FileUpload file) {
        Notiz existingNotiz = notizService.getById(notizID);
        Kind kind = existingNotiz.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotAddEntityToInactiveKind(
                kind,
                new FileData()));
        Notiz updatedNotiz = notizService.addUploadedFile(notizID, file);
        return toJson(updatedNotiz);
    }

    @NonNull
    public NotizJson removeUploadedFile(@NonNull ID<Notiz> notizID, @NonNull ID<FileData> fileIdToRemove) {
        Notiz existingNotiz = notizService.getById(notizID);
        Kind kind = existingNotiz.getDossier().getKind();
        KindValidationUtil.throwErrorIfKindIsInactive(
            kind,
            AppValidationMessage.cannotRemoveEntityFromInactiveKind(
                kind,
                new FileData()));
        notizService.removeUploadedFile(existingNotiz, fileIdToRemove);
        return toJson(existingNotiz);
    }
}
