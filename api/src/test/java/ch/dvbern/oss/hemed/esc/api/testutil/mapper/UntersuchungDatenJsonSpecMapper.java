package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungDatenJsonSpecMapper extends BaseMapper {

    UntersuchungDatenJsonSpecMapper INSTANCE = Mappers.getMapper( UntersuchungDatenJsonSpecMapper.class );

    @NonNull
    UntersuchungKindDatenJsonSpec untersuchungDatenJsonToUntersuchungDatenJsonSpec(@NonNull UntersuchungKindDatenJson json);}
