package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenMassnahmenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungImpfungenMassnahmenJsonSpecMapper extends BaseMapper {

    UntersuchungImpfungenMassnahmenJsonSpecMapper
			INSTANCE = Mappers.getMapper( UntersuchungImpfungenMassnahmenJsonSpecMapper.class );

    @NonNull
    UntersuchungImpfungenMassnahmenJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungImpfungenMassnahmenJson json
    );}
