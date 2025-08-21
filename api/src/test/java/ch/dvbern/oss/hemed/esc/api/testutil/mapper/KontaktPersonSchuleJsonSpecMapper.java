package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KontaktPersonSchuleJsonSpecMapper extends BaseMapper {

    KontaktPersonSchuleJsonSpecMapper INSTANCE = Mappers.getMapper(KontaktPersonSchuleJsonSpecMapper.class);

    @NonNull
    KontaktPersonSchuleJsonSpec kontaktPersonSchuleJsonToKontaktPersonSchuleJsonSpec(
        @NonNull KontaktPersonSchuleJson json);
}
