package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungDurchfuehrungJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungDurchfuehrungJsonSpecMapper extends BaseMapper {

    UntersuchungDurchfuehrungJsonSpecMapper INSTANCE = Mappers.getMapper( UntersuchungDurchfuehrungJsonSpecMapper.class );

    @NonNull
    UntersuchungDurchfuehrungJsonSpec untersuchungDurchfuehrungJsonToUntersuchungDurchfuehrungJsonSpec(
        @NonNull UntersuchungDurchfuehrungJson json
    );}
