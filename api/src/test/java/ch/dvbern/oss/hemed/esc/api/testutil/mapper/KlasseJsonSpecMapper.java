package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KlasseJsonSpecMapper extends BaseMapper {

    KlasseJsonSpecMapper INSTANCE = Mappers.getMapper(KlasseJsonSpecMapper.class);

    @NonNull
    KlasseJsonSpec klasseJsonToKlasseJsonSpec(@NonNull KlasseJson json);

    @Override
    @Nullable
    default String map(@Nullable ID<?> id) {
        return id != null ? id.getId() : null;
    }
}
