package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ImpfstoffChargeJsonSpecMapper extends BaseMapper {

    ImpfstoffChargeJsonSpecMapper INSTANCE = Mappers.getMapper( ImpfstoffChargeJsonSpecMapper.class );

    @NonNull
    ImpfstoffChargeJsonSpec impfstoffChargeJsonToImpfstoffChargeJsonSpec(@NonNull ImpfstoffChargeJson json);}
