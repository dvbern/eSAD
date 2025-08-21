package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
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
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    @NonNull
    public static AufgabeJson toJson(@NonNull Aufgabe aufgabe) {
        Kind kind = aufgabe.getKind();
        Anlass anlass = aufgabe.getAnlass();
        return new AufgabeJson(
            aufgabe.getId(),
            EntityInfoJson.fromEntity(aufgabe),
            new AufgabeSaveJson(
                aufgabe.getTitel(),
                aufgabe.getBemerkung(),
                aufgabe.getAufgabeStatus(),
                aufgabe.getFaelligBis(),
                BenutzerMapper.toOptionJson(aufgabe.getEmpfaenger()),
                BenutzerMapper.toOptionJson(aufgabe.getUserErfasst()),
                aufgabe.getLabels().stream().map(LabelMapper::toJson).toList(),
                kind != null ? SearchMapper.toSearchJson(kind) : null,
                anlass != null ? AnlassMapper.toOptionJson(anlass) : null,
                aufgabe.getFiles().stream().map(FileDataMapper::toJson).toList(),
                aufgabe.getLink()
            )
        );
    }

    @NonNull
    private Aufgabe toEntity(@NonNull Aufgabe existingAufgabe, @NonNull AufgabeSaveJson saveJson) {
        existingAufgabe.setTitel(saveJson.getTitel());
        existingAufgabe.setBemerkung(saveJson.getBemerkungen());
        existingAufgabe.setAufgabeStatus(saveJson.getStatus());
        existingAufgabe.setFaelligBis(saveJson.getFaelligBis());
        existingAufgabe.setUserErfasst(benutzerService.getById(saveJson.getUserErfasst().getId()));
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
        Benutzer erfasser = this.benutzerService.getById(saveJson.getUserErfasst().getId());
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
    public AufgabeListJson findAll() {
        List<Aufgabe> aufgaben = aufgabeService.findAll();
        // since we have to load all anyway we might es well count here to avoid another query
        List<AufgabeJson> aufgabenJson = aufgaben.stream()
            .map(AufgabeMapper::toJson)
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }

    @NonNull
    public AufgabeListJson findAllForKind(@NonNull ID<Kind> kindId) {
        List<AufgabeJson> aufgabenJson = aufgabeService.findAllForKind(kindId)
            .stream()
            .map(AufgabeMapper::toJson)
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }

    @NonNull
    public AufgabeJson getById(@NonNull ID<Aufgabe> aufgabeId) {
        return toJson(aufgabeService.getById(aufgabeId));
    }

    @NonNull
    public AufgabeJson create(@NonNull AufgabeSaveJson aufgabeJson) {
        Aufgabe entity = toEntityForCreate(aufgabeJson);
        return toJson(aufgabeService.persist(entity));
    }

    @NonNull
    public AufgabeJson update(@NonNull ID<Aufgabe> aufgabeID, @NonNull AufgabeSaveJson aufgabeJson) {
        Aufgabe existingAufgabe = aufgabeService.getById(aufgabeID);
        Aufgabe entity = toEntity(existingAufgabe, aufgabeJson);
        return toJson(aufgabeService.persist(entity));
    }

    int getNumOfOffeneFaelligeAufgaben() {
        return aufgabeService.getNumOfOffeneFaelligeAufgaben();
    }

    public int getNumOfFaelligeAufgabenForCurrentUser() {
        return aufgabeService.getNumOfOffeneFaelligeAufgabenByCurrentUser();
    }

    public void remove(@NonNull ID<Aufgabe> aufgabeID) {
        aufgabeService.remove(aufgabeID);
    }

    @NonNull
    public AufgabeJson addUploadedFile(@NonNull ID<Aufgabe> aufgabeID, @NonNull FileUpload file) {
        Aufgabe updatedAufgabe = aufgabeService.addUploadedFile(aufgabeID, file);
        return toJson(aufgabeService.persist(updatedAufgabe));
    }

    @NonNull
    public AufgabeJson removeUploadedFile(@NonNull ID<Aufgabe> aufgabeID, @NonNull ID<FileData> fileIdToRemove) {
        Aufgabe existingAufgabe = aufgabeService.getById(aufgabeID);
        aufgabeService.removeUploadedFile(existingAufgabe, fileIdToRemove);
        return toJson(existingAufgabe);
    }

    @NonNull
    public AufgabeListJson findAllNichtAbgeschlosseneAufgabenForCurrent() {

        List<AufgabeJson> aufgabenJson = aufgabeService.findAllNichtAbgeschlosseneAufgabenForCurrent()
            .stream()
            .map(AufgabeMapper::toJson)
            .toList();
        long anzahlEigeneFaelligeAufgaben = getNumOfFaelligeAufgabenForCurrentUser();

        return new AufgabeListJson(aufgabenJson, anzahlEigeneFaelligeAufgaben);
    }
}
