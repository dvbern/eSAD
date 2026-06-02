package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatSaveJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungBewegungsapparatSaveJsonSpecMapper extends BaseMapper {

    UntersuchungBewegungsapparatSaveJsonSpecMapper
        INSTANCE = Mappers.getMapper(UntersuchungBewegungsapparatSaveJsonSpecMapper.class);

    @NonNull
    UntersuchungBewegungsapparatSaveJsonSpec saveJsonToSaveJsonSpec(
        @NonNull UntersuchungBewegungsapparatSaveJson saveJson);
}
