package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KindJsonSpecMapper extends BaseMapper {

    KindJsonSpecMapper INSTANCE = Mappers.getMapper(KindJsonSpecMapper.class);

    @NonNull
    KindJsonSpec kindJsonToKindJsonSpec(@NonNull KindJson kindJson);
}
