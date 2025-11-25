package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json.UntersuchungGroesseGewichtJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UntersuchungGroesseGewichtJsonSpecMapper extends BaseMapper {
    UntersuchungGroesseGewichtJsonSpecMapper INSTANCE = Mappers.getMapper(UntersuchungGroesseGewichtJsonSpecMapper.class);

    @NonNull
    UntersuchungGroesseGewichtJsonSpec untersuchungGroesseGewichtJsonToUntersuchungGroesseGewichtJsonSpec(
        @NonNull UntersuchungGroesseGewichtJson json
    );
}
