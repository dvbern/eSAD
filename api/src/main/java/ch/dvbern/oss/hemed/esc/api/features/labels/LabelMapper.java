package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.OptimisticLockingService;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedStringMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class LabelMapper {

    private final LabelService labelService;
    private final OptimisticLockingService optimisticLockingService;
    private final TranslatedStringMapper translatedStringMapper;

    @NonNull
    public static LabelJson toJson(@NonNull Label label) {
        return new LabelJson(
            label.getId(),
            EntityInfoJson.fromEntity(label),
            new LabelSaveJson(
                TranslatedStringMapper.toJson(label.getName()),
                label.getLabelTyp(),
                label.isAutoLabel(),
                label.isDisabled(),
                label.isAktiv(),
                label.getVersion()
            )
        );
    }

    @NonNull
    private Label toEntityForCreate(@NonNull LabelSaveJson labelJson) {
        TranslatedString translatedString = translatedStringMapper.toEntity(null, labelJson.getName());
        return new Label(
            translatedString,
            labelJson.getLabelTyp(),
            false,
            false,
            true
        );
    }

    @NonNull
    private Label toEntity(@NonNull Label existingLabel, @NonNull LabelSaveJson labelJson) {
        TranslatedString translatedString =
            translatedStringMapper.toEntity(existingLabel.getName(), labelJson.getName());
        existingLabel.setName(translatedString);
        existingLabel.setLabelTyp(labelJson.getLabelTyp());
        existingLabel.setAktiv(labelJson.isAktiv());
        // The autoLabel and disabled properties should not be updatable.
        return existingLabel;
    }

    @NonNull
    public Set<Label> toEntityList(@NonNull List<LabelJson> labelJsonList) {
        return labelJsonList.stream()
            .map(LabelJson::getId)
            .map(labelService::getById)
            .collect(Collectors.toSet());
    }

    @NonNull
    public LabelJson create(@NonNull LabelSaveJson labelJson) {
        Label entity = toEntityForCreate(labelJson);
        return toJson(labelService.persist(entity));
    }

    @NonNull
    public LabelJson update(@NonNull ID<Label> labelID, @NonNull LabelSaveJson labelJson) {
        Label existingLabel = labelService.getById(labelID);
        if (existingLabel.isAutoLabel()) {
            throw new AppValidationException(AppValidationMessage.autoLabelsCannotBeEditedOrDeleted());
        }
        Label entity = toEntity(existingLabel, labelJson);
        Label updatedEntity = optimisticLockingService.persistWithOptimisticLocking(entity, labelJson);
        return toJson(updatedEntity);
    }

    public void remove(@NonNull ID<Label> labelID) {
        Label existingLabel = labelService.getById(labelID);
        if (existingLabel.isAutoLabel()) {
            throw new AppValidationException(AppValidationMessage.autoLabelsCannotBeEditedOrDeleted());
        }
        labelService.remove(labelID);
    }

    @NonNull
    public List<LabelJson> findAll() {
        return labelService.findAll().stream()
            .map(LabelMapper::toJson)
            .toList();
    }

    @NonNull
    public List<LabelJson> filterByType(@NonNull LabelTyp type) {
        return labelService.filterByType(type).stream()
            .map(LabelMapper::toJson)
            .toList();
    }

    public void requireNoDisabledOrInactivLabelAddedOrRemoved(
        @NonNull Set<Label> labelsBefore,
        @NonNull Set<Label> labelsNew
    ) {
        Set<Label> disabledOrInaktivLabelsBefore = labelsBefore.stream()
            .filter(l -> l.isDisabled() || !l.isAktiv()).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Label> disabledOrInaktivLabelsNew = labelsNew.stream()
            .filter(l -> l.isDisabled() || !l.isAktiv()).collect(Collectors.toCollection(LinkedHashSet::new));
        boolean added = disabledOrInaktivLabelsNew.stream()
            .anyMatch(label -> !disabledOrInaktivLabelsBefore.contains(label));
        boolean removed = disabledOrInaktivLabelsBefore.stream()
            .anyMatch(label -> !disabledOrInaktivLabelsNew.contains(label));
        if (added || removed) {
            throw new AppValidationException(AppValidationMessage.autoLabelsCannotBeAddedOrRemovedFromNotizOrAufgabe());
        }
    }
}
