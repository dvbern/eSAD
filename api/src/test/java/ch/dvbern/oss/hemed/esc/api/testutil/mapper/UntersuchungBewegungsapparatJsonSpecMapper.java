package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungBewegungsapparatJsonSpecMapper extends BaseMapper {

    UntersuchungBewegungsapparatJsonSpecMapper INSTANCE =
        Mappers.getMapper(UntersuchungBewegungsapparatJsonSpecMapper.class);

    @NonNull
    UntersuchungBewegungsapparatJsonSpec jsonToJsonSpec(
        @NonNull UntersuchungBewegungsapparatJson json);
}
