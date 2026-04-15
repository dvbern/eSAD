package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungVorabklaerungenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungVorabklaerungenJsonSpecMapper extends BaseMapper {
    UntersuchungVorabklaerungenJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungVorabklaerungenJsonSpecMapper.class);

    @NonNull
    UntersuchungVorabklaerungenJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungVorabklaerungenJson json
    );
}
