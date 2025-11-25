package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.json.UntersuchungLaeusekontrolleJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungLaeusekontrolleJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungLaeusekontrolleJsonSpecMapper extends BaseMapper {

    UntersuchungLaeusekontrolleJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungLaeusekontrolleJsonSpecMapper.class);

    @NonNull
    UntersuchungLaeusekontrolleJsonSpec toUntersuchungLaeusekontrolleJsonSpec(
        @NonNull UntersuchungLaeusekontrolleJson json);
}
