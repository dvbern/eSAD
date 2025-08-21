package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InfomaterialJsonSpecMapper extends BaseMapper {

    InfomaterialJsonSpecMapper INSTANCE = Mappers.getMapper(InfomaterialJsonSpecMapper.class);

    @NonNull
    InfomaterialJsonSpec infomaterialJsonToInfomaterialJsonSpec(@NonNull InfomaterialJson benutzerDTO);

}
