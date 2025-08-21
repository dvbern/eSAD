package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArztJsonSpecMapper extends BaseMapper {

    ArztJsonSpecMapper INSTANCE = Mappers.getMapper(ArztJsonSpecMapper.class);

    @NonNull
    ArztJsonSpec arztJsonToArztJsonSpec(@NonNull ArztJson arztJson);
}
