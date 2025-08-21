package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LehrpersonJsonSpecMapper extends BaseMapper {

    LehrpersonJsonSpecMapper INSTANCE = Mappers.getMapper(LehrpersonJsonSpecMapper.class);

    @NonNull
    LehrpersonJsonSpec lehrpersonJsonToLehrpersonJsonSpec(
        @NonNull LehrpersonJson json);
}
