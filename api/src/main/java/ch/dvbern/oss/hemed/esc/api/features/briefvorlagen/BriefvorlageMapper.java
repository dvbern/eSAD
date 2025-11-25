package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.arzt.ArztService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.BriefgenerierungService;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageJson;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageListJson;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataMapper;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@JaxMapper
@RequiredArgsConstructor
@Slf4j
public final class BriefvorlageMapper {

    private final BriefvorlageService briefvorlageService;
    private final BriefgenerierungService briefgenerierungService;
    private final KindService kindService;
    private final EinladungService einladungService;
    private final ArztService arztService;

    @NonNull
    public static BriefvorlageJson toJson(@NonNull Briefvorlage briefvorlage) {

        return new BriefvorlageJson(
            briefvorlage.getId(),
            EntityInfoJson.fromEntity(briefvorlage),
            new BriefvorlageSaveJson(
                briefvorlage.getName(),
                briefvorlage.getType(),
                briefvorlage.getSprache(),
                briefvorlage.getVersion()
            ),
            FileDataMapper.toJson(briefvorlage.getFile())
        );
    }

    @NonNull
    public BriefvorlageListJson findAll() {
        List<BriefvorlageJson> briefvorlagenJson = briefvorlageService.findAll()
            .stream()
            .map(BriefvorlageMapper::toJson)
            .toList();

        return new BriefvorlageListJson(briefvorlagenJson);
    }

    @NonNull
    public BriefvorlageJson create(
        @NonNull BriefvorlageSaveJson briefvorlageSaveJson,
        @NonNull FileUpload file
    ) {
        Briefvorlage entity = toEntityForCreate(briefvorlageSaveJson);
        FileData fileData = briefvorlageService.saveBriefvorlageAttachmentInFilestorage(file);
        entity.setFile(fileData);
        Briefvorlage savedBriefvorlage = briefvorlageService.persist(entity);
        return toJson(savedBriefvorlage);
    }

    @NonNull
    public BriefvorlageJson update(
        @NonNull ID<Briefvorlage> briefvorlageID,
        @NonNull BriefvorlageSaveJson briefvorlageSaveJson,
        @Nullable FileUpload file
    ) {
        Briefvorlage existingBriefvorlage = briefvorlageService.getById(briefvorlageID);
        Briefvorlage entity =
            toEntity(existingBriefvorlage, briefvorlageSaveJson);
        if (file != null) { // change filedata
            briefvorlageService.removeBriefvorlageAttachmentInFilestorage(entity.getFile());
            FileData fileData = briefvorlageService.saveBriefvorlageAttachmentInFilestorage(file);
            // Orphan removal deletes the old FileData.
            entity.setFile(fileData);
        }

        Briefvorlage savedBriefvorlage = briefvorlageService.persistWithOptimisticLocking(entity, briefvorlageSaveJson.getVersion());
        return toJson(savedBriefvorlage);
    }

    public void remove(@NonNull ID<Briefvorlage> briefvorlageID) {
        briefvorlageService.remove(briefvorlageID);
    }

    @NonNull
    public PrintResultDTO generateBrief(
        @NonNull ID<Briefvorlage> briefvorlageId,
        @NonNull ID<Kind> kindId,
        @Nullable ID<Einladung> einladungId,
        @Nullable ID<Arzt> arztId
    ) {
        Briefvorlage briefvorlage = briefvorlageService.getById(briefvorlageId);
        PrintResultDTO result = null;
        Arzt arzt = arztId != null ? arztService.getById(arztId) : null;

        switch (briefvorlage.getType()) {
            case ANLASS -> {
                Einladung einladung = einladungService.getById(Objects.requireNonNull(einladungId));
                result = briefgenerierungService.generateBriefForAnlass(briefvorlage, einladung, arzt);
            }
            case KIND -> {
                Kind kind = kindService.getById(kindId);
                result = briefgenerierungService.generateBriefForKind(briefvorlage, kind, arzt);
            }
            default ->
                throw new IllegalStateException("Unexpected value: " + briefvorlage.getType());
        }
        LOG.info("ESC-BRIEF-DOCMERGE: A Document was generated useing docmerge. kindId={}, einladungId={}, "
                + "arztId={}, briefvorlage={}",
            kindId.getUUID().toString(), einladungId, arztId, briefvorlage.toString());
        return result;
    }

    @NonNull
    public PrintResultDTO generateTestBrief(
        @NonNull ID<Briefvorlage> briefvorlageId
    ) {
        Briefvorlage briefvorlage = briefvorlageService.getById(briefvorlageId);
        return briefgenerierungService.generateTestBrief(briefvorlage);
    }

    @NonNull
    private Briefvorlage toEntityForCreate(@NonNull BriefvorlageSaveJson briefvorlageJson) {
        return new Briefvorlage(
            briefvorlageJson.getName(),
            briefvorlageJson.getType(),
            briefvorlageJson.getSprache(),
            new FileData()
        );
    }

    @NonNull
    private Briefvorlage toEntity(
        @NonNull Briefvorlage existingBriefvorlage,
        @NonNull BriefvorlageSaveJson briefvorlageJson
    ) {
        existingBriefvorlage.setName(briefvorlageJson.getName());
        existingBriefvorlage.setType(briefvorlageJson.getType());
        existingBriefvorlage.setSprache(briefvorlageJson.getSprache());
        return existingBriefvorlage;
    }
}
