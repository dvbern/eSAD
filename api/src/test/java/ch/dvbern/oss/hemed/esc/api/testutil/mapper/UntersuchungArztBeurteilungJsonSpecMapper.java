package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztBeurteilungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungArztBeurteilungJsonSpecMapper extends BaseMapper {
    UntersuchungArztBeurteilungJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungArztBeurteilungJsonSpecMapper.class);

    @NonNull
    UntersuchungArztBeurteilungJsonSpec untersuchungArztJsonToUntersuchungArztJsonSpec(
        @NonNull UntersuchungArztBeurteilungJson json
    );
}
