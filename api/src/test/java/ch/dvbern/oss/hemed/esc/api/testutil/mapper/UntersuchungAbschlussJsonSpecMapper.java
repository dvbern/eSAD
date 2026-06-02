package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungAbschlussJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungAbschlussJsonSpecMapper extends BaseMapper {
    UntersuchungAbschlussJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungAbschlussJsonSpecMapper.class);

    @NonNull
    UntersuchungAbschlussJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungAbschlussJson json
    );
}
