package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KlasseJsonSpecMapper extends BaseMapper {

    KlasseJsonSpecMapper INSTANCE = Mappers.getMapper(KlasseJsonSpecMapper.class);

    @NonNull
    KlasseJsonSpec klasseJsonToKlasseJsonSpec(@NonNull KlasseJson json);
}
