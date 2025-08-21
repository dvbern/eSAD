package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfstatusKrankheitSaveJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfstatusKrankheitSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfstatusKrankheitSaveJsonSpecMapper extends BaseMapper {

    UntersuchungImpfstatusKrankheitSaveJsonSpecMapper
			INSTANCE = Mappers.getMapper( UntersuchungImpfstatusKrankheitSaveJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfstatusKrankheitSaveJsonSpec untersuchungImpfstatusJsonToUntersuchungImpfstatusJsonSpec(
        @NonNull UntersuchungImpfstatusKrankheitSaveJson json
    );}
