package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBeurteilungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSehenBeurteilungJsonSpecMapper extends BaseMapper {
    UntersuchungSehenBeurteilungJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungSehenBeurteilungJsonSpecMapper.class);

    @NonNull
    UntersuchungSehenBeurteilungJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungSehenBeurteilungJson json
    );
}
