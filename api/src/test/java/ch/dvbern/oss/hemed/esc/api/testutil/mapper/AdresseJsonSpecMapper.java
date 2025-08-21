package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AdresseJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdresseJsonSpecMapper extends BaseMapper {

    AdresseJsonSpecMapper INSTANCE = Mappers.getMapper( AdresseJsonSpecMapper.class );

    @NonNull
    AdresseJsonSpec adresseJsonToAdresseJsonSpec(@NonNull AdresseJson json);}
