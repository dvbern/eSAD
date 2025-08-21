package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungenJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfungenJsonSpecMapper extends BaseMapper {

    UntersuchungImpfungenJsonSpecMapper INSTANCE = Mappers.getMapper( UntersuchungImpfungenJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfungenJsonSpec untersuchungImpfungenJsonToUntersuchungImpfungenJsonSpec(
        @NonNull UntersuchungImpfungenJson json
    );}
