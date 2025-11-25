package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AudiogrammJsonSpecMapper extends BaseMapper {
    AudiogrammJsonSpecMapper INSTANCE = Mappers.getMapper(AudiogrammJsonSpecMapper.class);

    @NonNull
    AudiogrammSaveJsonSpec audiogrammJsonToAudiogrammJsonSpec(@NonNull AudiogrammSaveJson json);
}
