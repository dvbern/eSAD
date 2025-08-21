package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSehenJsonSpecMapper extends BaseMapper {
    UntersuchungSehenJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungSehenJsonSpecMapper.class);

    @NonNull
    UntersuchungSehenJsonSpec untersuchungSehenJsonToUntersuchungSehenJsonSpec(
        @NonNull UntersuchungSehenJson json
    );
}
