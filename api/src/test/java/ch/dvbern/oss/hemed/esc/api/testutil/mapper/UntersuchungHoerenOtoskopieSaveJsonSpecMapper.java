package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungHoerenOtoskopieSaveJsonSpecMapper extends BaseMapper {
    UntersuchungHoerenOtoskopieSaveJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungHoerenOtoskopieSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungHoerenOtoskopieSaveJsonSpec saveJsonToSaveJsonSpec(
        @NonNull UntersuchungHoerenOtoskopieSaveJson saveJson
    );
}
