package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LabelJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LabelJsonSpecMapper extends BaseMapper {

    LabelJsonSpecMapper INSTANCE = Mappers.getMapper(LabelJsonSpecMapper.class);

    @NonNull
    LabelJsonSpec labelJsonToLabelJsonSpec(@NonNull LabelJson labelJson);
}
