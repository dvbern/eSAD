package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungInternJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfungInternJsonSpecMapper extends BaseMapper {

    UntersuchungImpfungInternJsonSpecMapper
			INSTANCE = Mappers.getMapper( UntersuchungImpfungInternJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfungInternJsonSpec untersuchungImpfungInternJsonToUntersuchungImpfungInternJsonSpec(
        @NonNull UntersuchungImpfungInternJson json
    );}
