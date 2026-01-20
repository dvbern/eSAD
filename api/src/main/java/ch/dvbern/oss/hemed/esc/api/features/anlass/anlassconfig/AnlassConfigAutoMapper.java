package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.BeanMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public interface AnlassConfigAutoMapper extends BaseMapper {

    AnlassConfigAutoMapper INSTANCE = Mappers.getMapper(AnlassConfigAutoMapper.class);

    /**
     * This maps data from the json to the entity. The gueltigkeitsZeitraum and the anlassTyp can NEVER be updated*
     */
//    @Mapping(target = "gueltigkeitsZeitraum", ignore = true)
    @Mapping(source = "datumVon", target = "gueltigkeitsZeitraum.datumVon", ignore = true)
    @Mapping(source = "datumBis", target = "gueltigkeitsZeitraum.datumBis", ignore = true)
    @Mapping(target = "anlassTyp" , source = "anlassTyp", ignore = true)
    @BeanMapping( unmappedSourcePolicy = ReportingPolicy.ERROR)
    void updateAnlassConfigFromAnlassConfigSaveJson(
        @MappingTarget AnlassConfig entity,
        @NonNull AnlassConfigSaveJson json
    );

    AnlassConfig toEntityForCreate(@NonNull AnlassConfigSaveJson saveJson);


    /**
     * this maps data from the entity to the json, if there are unmapped properties in the json
     * that will be an error
     */
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "datumVon", source = "gueltigkeitsZeitraum.datumVon")
    @Mapping(target = "datumBis", source = "gueltigkeitsZeitraum.datumBis")
    AnlassConfigSaveJson toSaveJson(@NonNull AnlassConfig entity);

}
