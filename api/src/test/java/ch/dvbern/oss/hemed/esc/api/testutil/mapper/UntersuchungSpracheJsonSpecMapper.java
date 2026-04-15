package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSpracheJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSpracheJsonSpecMapper extends BaseMapper {
    UntersuchungSpracheJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungSpracheJsonSpecMapper.class);

    @NonNull
    UntersuchungSpracheJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungSpracheJson json
    );
}
