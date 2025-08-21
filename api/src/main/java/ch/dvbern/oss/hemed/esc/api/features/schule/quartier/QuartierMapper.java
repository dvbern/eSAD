package ch.dvbern.oss.hemed.esc.api.features.schule.quartier;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class QuartierMapper {

    private final QuartierService quartierService;

    @NonNull
    private Quartier toEntity(
        @NonNull Quartier existingQuartier,
        @NonNull QuartierSaveJson quartierSaveJson
    ) {
        existingQuartier.setQuartierName(quartierSaveJson.getQuartierName());
        return existingQuartier;
    }

    @Nullable
    public Quartier toEntity(
        @Nullable QuartierOptionJson quartierOptionJson
    ) {
        if (quartierOptionJson == null) {
            return null;
        }
        return this.quartierService.getById(quartierOptionJson.getId());
    }

    @NonNull
    private Quartier toEntityForCreate(@NonNull QuartierSaveJson quartierSaveJson) {
        return new Quartier(
            quartierSaveJson.getQuartierName()
        );
    }

    @NonNull
    public static QuartierJson toJson(@NonNull Quartier quartier) {
        return new QuartierJson(
            quartier.getId(),
            EntityInfoJson.fromEntity(quartier),
            new QuartierSaveJson(
                quartier.getQuartierName()
            )
        );
    }

    @Nullable
    public static QuartierOptionJson toOptionJson(@Nullable Quartier quartier) {
        if (quartier == null) {
            return null;
        }
        return new QuartierOptionJson(
            quartier.getId(),
            quartier.getQuartierName()
        );
    }

    @NonNull
    public List<QuartierJson> findAll() {
        return quartierService.findAll().stream()
            .map(QuartierMapper::toJson)
            .toList();
    }

    @NonNull
    public List<QuartierOptionJson> findAllOptions() {
        return quartierService.findAll().stream()
            .map(QuartierMapper::toOptionJson)
            .toList();
    }
}
