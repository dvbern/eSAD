package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AudiogrammJsonSpecMapper extends BaseMapper {
    AudiogrammJsonSpecMapper INSTANCE = Mappers.getMapper(AudiogrammJsonSpecMapper.class);

    @NonNull
    AudiogrammJsonSpec audiogrammJsonToAudiogrammJsonSpec(@NonNull AudiogrammJson json);
}
