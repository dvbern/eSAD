package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json.EinladungJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EinladungJsonSpecMapper extends BaseMapper {

    EinladungJsonSpecMapper INSTANCE = Mappers.getMapper( EinladungJsonSpecMapper.class );

    @NonNull
    EinladungJsonSpec einladungJsonToEinladungJsonSpec(@NonNull EinladungJson einladungJson);

    @Override
    @Nullable
    default String map(@Nullable ID<?> id) {
        return id != null ? id.getId() : null;
    }
}
