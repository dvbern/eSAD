package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSignaturJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSignaturJsonSpecMapper extends BaseMapper {
    UntersuchungSignaturJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungSignaturJsonSpecMapper.class);

    @NonNull
    UntersuchungSignaturJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungSignaturJson json
    );
}
