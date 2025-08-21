package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SchuleJsonSpecMapper extends BaseMapper {

    SchuleJsonSpecMapper INSTANCE = Mappers.getMapper(SchuleJsonSpecMapper.class);

    @NonNull
    SchuleJsonSpec schuleJsonToSchuleJsonSpec(@NonNull SchuleJson json);}
