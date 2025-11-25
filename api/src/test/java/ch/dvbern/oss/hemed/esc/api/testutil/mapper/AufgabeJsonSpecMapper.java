package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AufgabeJsonSpecMapper extends BaseMapper {

    AufgabeJsonSpecMapper INSTANCE = Mappers.getMapper(AufgabeJsonSpecMapper.class);

    @NonNull
    AufgabeJsonSpec aufgabeJsonToAufgabeJsonSpec(@NonNull AufgabeJson aufgabeJson);
}
