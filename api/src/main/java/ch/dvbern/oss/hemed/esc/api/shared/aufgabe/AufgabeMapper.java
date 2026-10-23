package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelMapper;
import ch.dvbern.oss.hemed.esc.api.features.search.SearchMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeListJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class AufgabeMapper {

    private final AufgabeService aufgabeService;
    private final BenutzerService benutzerService;
    private final KindService kindService;
    private final AnlassService anlassService;
    private final LabelMapper labelMapper;
    private final TenantContext tenantContext;
    private final TL tl;

    @NonNull
    public static AufgabeJson toJson(
        @NonNull Aufgabe aufgabe,
        @NonNull TenantEnum tenant,
        @Nullable AnlassOptionJson anlassOptionJson
    ) {
        Kind kind = aufgabe.getKind();
        return new AufgabeJson(
            aufgabe.getId(),
            EntityInfoJson.fromEntity(aufgabe),
            new AufgabeSaveJson(
                aufgabe.getTitel(),
                aufgabe.getBemerkung(),
                aufgabe.getAufgabeStatus(),
                aufgabe.getFaelligBis(),
                BenutzerMapper.toOptionJson(aufgabe.getEmpfaenger(), tenant),
                BenutzerMapper.toOptionJson(aufgabe.getUserErfasst(), tenant),
                aufgabe.getLabels().stream().map(LabelMapper::toJson).toList(),
                kind != null ? SearchMapper.toSearchJson(kind) : null,
                anlassOptionJson,
                aufgabe.getFiles().stream().map(FileDataMapper::toJson).toList(),
                aufgabe.getLink(),
                aufgabe.getVersion()
            )
        );
    }

    @NonNull
    private Aufgabe toEntity(@NonNull Aufgabe existingAufgabe, @NonNull AufgabeSaveJson saveJson) {
        existingAufgabe.setTitel(saveJson.getTitel());
        existingAufgabe.setBemerkung(saveJson.getBemerkungen());
        existingAufgabe.setAufgabeStatus(saveJson.getStatus());
        existingAufgabe.setFaelligBis(saveJson.getFaelligBis());
        // User Erfasst wird nicht angepasst im update
        existingAufgabe.setEmpfaenger(benutzerService.getById(saveJson.getEmpfaenger().getId()));
        Set<Label> desiredLabels = labelMapper.toEntityList(saveJson.getLabels());
        labelMapper.requireNoDisabledOrInactivLabelAddedOrRemoved(existingAufgabe.getLabels(), desiredLabels);
        existingAufgabe.getLabels().clear();
        existingAufgabe.getLabels().addAll(desiredLabels);
        Kind kind = saveJson.getKind() != null ? kindService.getById(saveJson.getKind().getId()) : null;
        existingAufgabe.setKind(kind);
        existingAufgabe.setLink(saveJson.getLink());
        return existingAufgabe;
    }

    @NonNull
    private Aufgabe toEntityForCreate(@NonNull AufgabeSaveJson saveJson) {
        Benutzer empfaenger = this.benutzerService.getById(saveJson.getEmpfaenger().getId());
        Benutzer erfasser = this.benutzerService.getCurrentBenutzerOrThrowException();
        Kind kind = saveJson.getKind() != null ? this.kindService.getById(saveJson.getKind().getId()) : null;
        Anlass anlass =
            saveJson.getAnlass() != null ? this.anlassService.getById(saveJson.getAnlass().getAnlassId()) : null;
        Set<Label> desiredLabels = labelMapper.toEntityList(saveJson.getLabels());
        return new Aufgabe(
            saveJson.getTitel(),
            saveJson.getBemerkungen(),
            saveJson.getStatus(),
            saveJson.getFaelligBis(),
            erfasser,
            empfaenger,
            desiredLabels,
            kind,
            anlass,
            // Files will be handled separately
            new ArrayList<>(),
            saveJson.getLink()
        );
    }

    @NonNull
    public AufgabeListJson findAllAufgabenBerechtigt(@NonNull Predicate<Aufgabe> hasReadAuthorizationAufgabe) {
        List<Aufgabe> aufgaben = aufgabeService.findAll();
        // since we have to load all anyway we might es well count here to avoid another query
        List<AufgabeJson> aufgabenJson = aufgaben.stream()
            .filter(hasReadAuthorizationAufgabe)
            .map(a -> AufgabeMapper.toJson(
                a,
                tenantContext.get(),
                Objects.nonNull(a.getAnlass()) ? AnlassMapper.toOptionJson(a.getAnlass(), tl) : null
            ))
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }

    @NonNull
    public AufgabeListJson findAllForKind(
        @NonNull ID<Kind> kindId,
        @NonNull Predicate<Aufgabe> hasReadAuthorizationAufgabe) {
        List<AufgabeJson> aufgabenJson = aufgabeService.findAllForKind(kindId)
            .stream()
            .filter(hasReadAuthorizationAufgabe)
            .map(a -> AufgabeMapper.toJson(
                a,
                tenantContext.get(),
                Objects.nonNull(a.getAnlass()) ? AnlassMapper.toOptionJson(a.getAnlass(), tl) : null
            ))
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }

    @NonNull
    public AufgabeJson getById(
        @NonNull ID<Aufgabe> aufgabeId,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe aufgabe = aufgabeService.getById(aufgabeId);
        requireAuthorization.accept(aufgabe);
        return toJson(
            aufgabe,
            tenantContext.get(),
            Objects.nonNull(aufgabe.getAnlass()) ? AnlassMapper.toOptionJson(aufgabe.getAnlass(), tl) : null
        );
    }

    @NonNull
    public AufgabeJson create(
        @NonNull AufgabeSaveJson aufgabeJson,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe entity = toEntityForCreate(aufgabeJson);
        requireAuthorization.accept(entity);
        if (aufgabeJson.getKind() != null) {
            Kind kind = kindService.getById(aufgabeJson.getKind().getId());
            KindValidationUtil.throwErrorIfKindIsInactive(
                kind,
                AppValidationMessage.cannotCreateEntityForInactiveKind(
                    kind,
                    entity));
        }
        Aufgabe persisted = aufgabeService.persist(entity);
        return toJson(
            persisted,
            tenantContext.get(),
            Objects.nonNull(persisted.getAnlass()) ? AnlassMapper.toOptionJson(persisted.getAnlass(), tl) : null
        );
    }

    @NonNull
    public AufgabeJson update(
        @NonNull ID<Aufgabe> aufgabeID,
        @NonNull AufgabeSaveJson aufgabeJson,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe existingAufgabe = aufgabeService.getById(aufgabeID);
        requireAuthorization.accept(existingAufgabe);
        if (existingAufgabe.getKind() != null) {
            KindValidationUtil.throwErrorIfKindIsInactive(
                existingAufgabe.getKind(),
                AppValidationMessage.cannotEditEntityForInactiveKind(
                    existingAufgabe.getKind(),
                    existingAufgabe));
        }
        Aufgabe entity = toEntity(existingAufgabe, aufgabeJson);
        Aufgabe persisted = aufgabeService.persistWithOptimisticLocking(entity, aufgabeJson.getVersion());
        return toJson(
            persisted,
            tenantContext.get(),
            Objects.nonNull(persisted.getAnlass()) ? AnlassMapper.toOptionJson(persisted.getAnlass(), tl) : null
        );
    }

    int getNumOfOffeneFaelligeAufgaben(@NonNull Predicate<Aufgabe> hasReadAuthorizationAufgabe) {
        return Math.toIntExact(aufgabeService.getOffeneFaelligeAufgaben().stream()
            .filter(hasReadAuthorizationAufgabe)
            .count());
    }

    public int getNumOfFaelligeAufgabenForCurrentUser() {
        return aufgabeService.getNumOfOffeneFaelligeAufgabenByCurrentUser();
    }

    public void remove(
        @NonNull ID<Aufgabe> aufgabeID,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe aufgabe = aufgabeService.getById(aufgabeID);
        requireAuthorization.accept(aufgabe);
        if (aufgabe.getKind() != null) {
            KindValidationUtil.throwErrorIfKindIsInactive(
                aufgabe.getKind(),
                AppValidationMessage.cannotRemoveEntityFromInactiveKind(
                    aufgabe.getKind(),
                    aufgabe));
        }
        aufgabeService.remove(aufgabeID);
    }

    @NonNull
    public AufgabeJson addUploadedFile(
        @NonNull ID<Aufgabe> aufgabeID,
        @NonNull FileUpload file,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe aufgabe = aufgabeService.getById(aufgabeID);
        requireAuthorization.accept(aufgabe);
        if (aufgabe.getKind() != null) {
            KindValidationUtil.throwErrorIfKindIsInactive(
                aufgabe.getKind(),
                AppValidationMessage.cannotAddEntityToInactiveKind(
                    aufgabe.getKind(),
                    aufgabe));
        }
        Aufgabe updatedAufgabe = aufgabeService.addUploadedFile(aufgabeID, file);
        updatedAufgabe.disableOptimisticLockingCheck();
        Aufgabe persisted = aufgabeService.persistAndFlush(updatedAufgabe);
        return toJson(
            persisted,
            tenantContext.get(),
            Objects.nonNull(persisted.getAnlass()) ? AnlassMapper.toOptionJson(persisted.getAnlass(), tl) : null
        );
    }

    @NonNull
    public AufgabeJson removeUploadedFile(
        @NonNull ID<Aufgabe> aufgabeID,
        @NonNull ID<FileData> fileIdToRemove,
        @NonNull Consumer<Aufgabe> requireAuthorization
    ) {
        Aufgabe existingAufgabe = aufgabeService.getById(aufgabeID);
        if (existingAufgabe.getKind() != null) {
            KindValidationUtil.throwErrorIfKindIsInactive(
                existingAufgabe.getKind(),
                AppValidationMessage.cannotRemoveEntityFromInactiveKind(
                    existingAufgabe.getKind(),
                    new FileData()));
        }
        aufgabeService.removeUploadedFile(existingAufgabe, fileIdToRemove, requireAuthorization);
        return toJson(
            existingAufgabe,
            tenantContext.get(),
            Objects.nonNull(existingAufgabe.getAnlass()) ?
                AnlassMapper.toOptionJson(existingAufgabe.getAnlass(), tl) :
                null
        );
    }

    @NonNull
    public AufgabeListJson findAllNichtAbgeschlosseneAufgabenForCurrent() {

        List<AufgabeJson> aufgabenJson = aufgabeService.findAllNichtAbgeschlosseneAufgabenForCurrent()
            .stream()
            .map(a -> AufgabeMapper.toJson(
                a,
                tenantContext.get(),
                Objects.nonNull(a.getAnlass()) ? AnlassMapper.toOptionJson(a.getAnlass(), tl) : null
            ))
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }
}
