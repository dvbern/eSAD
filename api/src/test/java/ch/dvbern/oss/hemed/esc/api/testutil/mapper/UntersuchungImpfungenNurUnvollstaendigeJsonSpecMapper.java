package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenNurUnvollstaendigeJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenNurUnvollstaendigeJsonSpec;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfungenNurUnvollstaendigeJsonSpecMapper extends BaseMapper {

    UntersuchungImpfungenNurUnvollstaendigeJsonSpecMapper
			INSTANCE = Mappers.getMapper( UntersuchungImpfungenNurUnvollstaendigeJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfungenNurUnvollstaendigeJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungImpfungenNurUnvollstaendigeJson json
    );}
