package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KindJsonSpecMapper extends BaseMapper {

    KindJsonSpecMapper INSTANCE = Mappers.getMapper(KindJsonSpecMapper.class);

    @NonNull
    KindJsonSpec kindJsonToKindJsonSpec(@NonNull KindJson kindJson);

    @Override
    @Nullable
    default String map(@Nullable ID<?> id) {
        return id != null ? id.getId() : null;
    }
}
