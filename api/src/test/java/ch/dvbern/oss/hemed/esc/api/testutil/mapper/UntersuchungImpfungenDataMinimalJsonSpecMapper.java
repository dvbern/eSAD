package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenDataMinimalJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfungenDataMinimalJsonSpecMapper extends BaseMapper {

    UntersuchungImpfungenDataMinimalJsonSpecMapper
			INSTANCE = Mappers.getMapper( UntersuchungImpfungenDataMinimalJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfungenDataMinimalJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungImpfungenDataMinimalJson json
    );}
