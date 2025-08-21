package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DossierJsonSpecMapper extends BaseMapper {

    DossierJsonSpecMapper INSTANCE = Mappers.getMapper( DossierJsonSpecMapper.class );

    @NonNull
    DossierJsonSpec dossierJsonToDossierJsonSpec(@NonNull DossierJson dossierJson);}
