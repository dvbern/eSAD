package ch.dvbern.oss.hemed.esc.api.testutil.mapper;

import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsJson;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TenantSettingsJsonSpec;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TenantSettingJsonSpecMapper extends BaseMapper {

    TenantSettingJsonSpecMapper INSTANCE = Mappers.getMapper(TenantSettingJsonSpecMapper.class);

    @NonNull
    TenantSettingsJsonSpec toJsonSpec(@NonNull TenantSettingsJson aufgabeJson);
}
