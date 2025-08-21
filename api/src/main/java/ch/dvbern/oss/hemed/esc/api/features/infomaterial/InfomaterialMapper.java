package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedStringMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class InfomaterialMapper {

    private final InfomaterialService infomaterialService;
    private final TranslatedStringMapper translatedStringMapper;

    @NonNull
    public static InfomaterialJson toJson(@NonNull Infomaterial infomaterial) {
        return new InfomaterialJson(
            infomaterial.getId(), EntityInfoJson.fromEntity(infomaterial), new InfomaterialSaveJson(
            TranslatedStringMapper.toJson(infomaterial.getName()),
            infomaterial.isAktiv()));
    }

    @NonNull
    private Infomaterial toEntityForCreate(@NonNull InfomaterialSaveJson infomaterialJson) {
        TranslatedString translatedString = translatedStringMapper.toEntity(null, infomaterialJson.getName());
        return new Infomaterial(translatedString, infomaterialJson.isAktiv());
    }

    @NonNull
    private Infomaterial toEntity(
        @NonNull Infomaterial existingInfomaterial,
        @NonNull InfomaterialSaveJson infomaterialJson) {
        TranslatedString translatedString =
            this.translatedStringMapper.toEntity(existingInfomaterial.getName(), infomaterialJson.getName());
        existingInfomaterial.setName(translatedString);
        existingInfomaterial.setAktiv(infomaterialJson.isAktiv());
        return existingInfomaterial;
    }

    @NonNull
    public Set<Infomaterial> toEntityList(@NonNull List<InfomaterialJson> infomaterialJsonList) {
        return infomaterialJsonList.stream()
            .map(InfomaterialJson::getId)
            .map(infomaterialService::getById)
            .collect(Collectors.toSet());
    }

    @NonNull
    public InfomaterialJson create(@NonNull InfomaterialSaveJson saveJson) {
        Infomaterial entity = toEntityForCreate(saveJson);
        return toJson(infomaterialService.persist(entity));
    }

    @NonNull
    public InfomaterialJson update(
        @NonNull ID<Infomaterial> infomaterialID,
        @NonNull InfomaterialSaveJson infomaterialJson) {
        Infomaterial existingInfomaterial = infomaterialService.getById(infomaterialID);
        Infomaterial entity = toEntity(existingInfomaterial, infomaterialJson);
        return toJson(infomaterialService.persist(entity));
    }

    public void remove(@NonNull ID<Infomaterial> infomaterialID) {
        infomaterialService.remove(infomaterialID);
    }

    @NonNull
    public List<InfomaterialJson> findAll(
        boolean onlyActive
    ) {
        List<Infomaterial> infomaterialien;
        if (onlyActive) {
            infomaterialien = infomaterialService.findAllActive();
        } else {
            infomaterialien = infomaterialService.findAll();
        }

        return infomaterialien.stream().map(InfomaterialMapper::toJson).toList();
    }

    public static void requireNoInactivInfomaterialAddedOrRemoved(
        @NonNull Set<Infomaterial> infomaterialBefore,
        @NonNull Set<Infomaterial> infomaterialNew
    ) {
        Set<Infomaterial> inaktivBefore = infomaterialBefore.stream()
            .filter(i -> !i.isAktiv()).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Infomaterial> inaktivNew = infomaterialNew.stream()
            .filter(i -> !i.isAktiv()).collect(Collectors.toCollection(LinkedHashSet::new));
        boolean added = inaktivNew.stream()
            .anyMatch(infomaterial -> !inaktivBefore.contains(infomaterial));
        boolean removed = inaktivBefore.stream()
            .anyMatch(infomaterial -> !inaktivNew.contains(infomaterial));
        if (added || removed) {
            throw new AppValidationException(AppValidationMessage.inactiveInfomaterialCannotBeAddedOrRemoved());
        }
    }
}
