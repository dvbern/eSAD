package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungHoerenAudiogrammSaveJsonSpecMapper extends BaseMapper {
    UntersuchungHoerenAudiogrammSaveJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungHoerenAudiogrammSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungHoerenAudiogrammSaveJsonSpec saveJsonToSaveJsonSpec(
        @NonNull UntersuchungHoerenAudiogrammSaveJson saveJson
    );
}
