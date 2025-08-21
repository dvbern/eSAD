package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopieJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungHoerenOtoskopieJsonSpecMapper extends BaseMapper {
    UntersuchungHoerenOtoskopieJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungHoerenOtoskopieJsonSpecMapper.class);

    @NonNull
    UntersuchungHoerenOtoskopieJsonSpec untersuchungHoerenOtoskopieJsonToUntersuchungHoerenOtoskopieJsonSpec(
        @NonNull UntersuchungHoerenOtoskopieJson json
    );
}
