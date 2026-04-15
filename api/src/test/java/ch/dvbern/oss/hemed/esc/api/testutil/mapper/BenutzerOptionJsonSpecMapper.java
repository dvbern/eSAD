package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BenutzerOptionJsonSpecMapper extends BaseMapper {

	BenutzerOptionJsonSpecMapper INSTANCE = Mappers.getMapper(BenutzerOptionJsonSpecMapper.class);

	@NonNull
    BenutzerOptionJsonSpec benutzerOptionJsonToBenutzerOptionJsonSpec(@NonNull BenutzerOptionJson benutzerOptionJson);
}
