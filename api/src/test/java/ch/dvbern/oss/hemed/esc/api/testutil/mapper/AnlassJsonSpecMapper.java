package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnlassJsonSpecMapper extends BaseMapper {

    AnlassJsonSpecMapper INSTANCE = Mappers.getMapper(AnlassJsonSpecMapper.class);

    @NonNull
    AnlassJsonSpec anlassJsonToAnlassJsonSpec(@NonNull AnlassJson anlassJson);
}
