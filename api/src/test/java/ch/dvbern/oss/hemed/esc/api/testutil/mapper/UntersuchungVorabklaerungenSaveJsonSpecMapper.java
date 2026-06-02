package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungVorabklaerungenSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungVorabklaerungenSaveJsonSpecMapper extends BaseMapper {
    UntersuchungVorabklaerungenSaveJsonSpecMapper
        INSTANCE = Mappers.getMapper(UntersuchungVorabklaerungenSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungVorabklaerungenSaveJsonSpec saveJsonToSaveJsonSpec(
        @NonNull UntersuchungVorabklaerungenSaveJson saveJson
    );
}
