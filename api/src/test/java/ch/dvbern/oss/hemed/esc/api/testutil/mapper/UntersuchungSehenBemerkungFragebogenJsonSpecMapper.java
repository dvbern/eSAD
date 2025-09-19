package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenBemerkungFragebogenJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenBemerkungFragebogenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSehenBemerkungFragebogenJsonSpecMapper extends BaseMapper {
    UntersuchungSehenBemerkungFragebogenJsonSpecMapper
        INSTANCE = Mappers.getMapper(UntersuchungSehenBemerkungFragebogenJsonSpecMapper.class);

    @NonNull
    UntersuchungSehenBemerkungFragebogenJsonSpec untersuchungSehenJsonToUntersuchungSehenJsonSpec(
        @NonNull UntersuchungSehenBemerkungFragebogenJson json
    );
}
