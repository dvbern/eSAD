package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BenutzerJsonSpecMapper extends BaseMapper {

	BenutzerJsonSpecMapper INSTANCE = Mappers.getMapper(BenutzerJsonSpecMapper.class);

	@NonNull
	BenutzerJsonSpec benutzerJsonToBenutzerJsonSpec(@NonNull BenutzerJson benutzerJson);
}
