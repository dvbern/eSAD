package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSchulrelevanteBeeintraechtigungJsonSpecMapper extends BaseMapper {
    UntersuchungSchulrelevanteBeeintraechtigungJsonSpecMapper
            INSTANCE = Mappers.getMapper(UntersuchungSchulrelevanteBeeintraechtigungJsonSpecMapper.class);

    @NonNull
    UntersuchungSchulrelevanteBeeintraechtigungJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungSchulrelevanteBeeintraechtigungJson json
    );
}
