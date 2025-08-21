package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueDisplayJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueDisplayJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DocumentQueueJsonSpecMapper extends BaseMapper {

    DocumentQueueJsonSpecMapper INSTANCE = Mappers.getMapper( DocumentQueueJsonSpecMapper.class );

    @NonNull
    DocumentQueueDisplayJsonSpec documentQueueJsonToDocumentQueueJsonSpec(@NonNull DocumentQueueDisplayJson displayJson);}
