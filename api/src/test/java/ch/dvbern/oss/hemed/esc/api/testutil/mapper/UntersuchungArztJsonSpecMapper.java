package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungArztJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungArztJsonSpecMapper extends BaseMapper {
    UntersuchungArztJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungArztJsonSpecMapper.class);

    @NonNull
    UntersuchungArztJsonSpec untersuchungArztJsonToUntersuchungArztJsonSpec(
        @NonNull UntersuchungArztJson json
    );
}
