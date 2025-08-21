package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ImpfstoffJsonSpecMapper extends BaseMapper {

    ImpfstoffJsonSpecMapper INSTANCE = Mappers.getMapper( ImpfstoffJsonSpecMapper.class );

    @NonNull
    ImpfstoffJsonSpec impfstoffJsonToImpfstoffJsonSpec(@NonNull ImpfstoffJson json);}
