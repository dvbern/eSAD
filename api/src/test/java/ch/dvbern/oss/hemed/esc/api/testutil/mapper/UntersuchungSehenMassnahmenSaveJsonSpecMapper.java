package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungSehenMassnahmenSaveJsonSpecMapper extends BaseMapper {
    UntersuchungSehenMassnahmenSaveJsonSpecMapper
			INSTANCE = Mappers.getMapper(UntersuchungSehenMassnahmenSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungSehenMassnahmenSaveJsonSpec untersuchungMassnahmenJsonToUntersuchungMassnahmenJsonSpec(
        @NonNull UntersuchungSehenMassnahmenSaveJson json
    );
}
