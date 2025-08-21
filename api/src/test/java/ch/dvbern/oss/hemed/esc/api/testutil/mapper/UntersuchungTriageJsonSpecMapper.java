package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTriageJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTriageJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungTriageJsonSpecMapper extends BaseMapper {

    UntersuchungTriageJsonSpecMapper INSTANCE = Mappers.getMapper( UntersuchungTriageJsonSpecMapper.class );

    @NonNull
    UntersuchungTriageJsonSpec untersuchungTriageJsonToUntersuchungTriageJsonSpec(
        @NonNull UntersuchungTriageJson json
    );}
