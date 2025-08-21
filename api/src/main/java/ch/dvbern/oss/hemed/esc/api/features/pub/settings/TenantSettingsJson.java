package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class TenantSettingsJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<TenantSetting> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AppLanguages defaultLanguage;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final List<AppLanguages> supportedLanguages;
}
