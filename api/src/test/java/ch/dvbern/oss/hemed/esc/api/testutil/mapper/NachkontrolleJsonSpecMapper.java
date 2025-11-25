package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.kinder.json.NachkontrolleJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NachkontrolleJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NachkontrolleJsonSpecMapper extends BaseMapper {

    NachkontrolleJsonSpecMapper INSTANCE = Mappers.getMapper(NachkontrolleJsonSpecMapper.class);

    @NonNull
    NachkontrolleJsonSpec nachkontrolleJsonToNachkontrolleJsonSpec(@NonNull NachkontrolleJson nachkontrolleJson);
}
