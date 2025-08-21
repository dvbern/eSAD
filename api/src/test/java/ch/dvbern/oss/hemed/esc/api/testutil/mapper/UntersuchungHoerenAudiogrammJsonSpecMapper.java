package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogrammJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungHoerenAudiogrammJsonSpecMapper extends BaseMapper {
    UntersuchungHoerenAudiogrammJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungHoerenAudiogrammJsonSpecMapper.class);

    @NonNull
    UntersuchungHoerenAudiogrammJsonSpec untersuchungHoerenAudiogrammJsonToUntersuchungHoerenAudiogrammJsonSpec(
        @NonNull UntersuchungHoerenAudiogrammJson json
    );
}
