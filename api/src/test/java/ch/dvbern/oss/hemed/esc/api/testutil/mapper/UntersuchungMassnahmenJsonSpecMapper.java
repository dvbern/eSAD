package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungMassnahmenJsonSpecMapper extends BaseMapper {
    UntersuchungMassnahmenJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungMassnahmenJsonSpecMapper.class);

    @NonNull
    UntersuchungMassnahmenJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungMassnahmenJson json
    );
}
