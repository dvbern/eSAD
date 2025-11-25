package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KontaktPersonKindJsonSpecMapper extends BaseMapper {

    KontaktPersonKindJsonSpecMapper INSTANCE = Mappers.getMapper(KontaktPersonKindJsonSpecMapper.class);

    @NonNull
    KontaktPersonKindJsonSpec kontaktPersonKindJsonToKontaktPersonKindJsonSpec(@NonNull KontaktPersonKindJson json);
}
