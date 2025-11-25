package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBemerkungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungArztBemerkungJsonSpecMapper extends BaseMapper {
    UntersuchungArztBemerkungJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungArztBemerkungJsonSpecMapper.class);

    @NonNull
    UntersuchungArztBemerkungJsonSpec untersuchungArztBemerkungJsonToUntersuchungArztBemerkungJsonSpec(
        @NonNull UntersuchungArztBemerkungJson json
    );
}
