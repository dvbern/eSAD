package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungKindDatenSaveJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungDatenKindSaveJsonSpecMapper extends BaseMapper {

    UntersuchungDatenKindSaveJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungDatenKindSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungKindDatenSaveJsonSpec untersuchungKindSaveJsonToUntersuchungKindDatenSaveJsonSpec(
        @NonNull
        UntersuchungKindDatenSaveJson json);
}
