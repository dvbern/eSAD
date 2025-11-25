package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TenantSettingTest {

    @Test
    void setSupportedLanguages_is_empty_if_empty() {
        var tenantSetting = new TenantSetting();
        tenantSetting.setSupportedLanguages(List.of());
        Assertions.assertThat(tenantSetting.getSupportedLanguages()).isEmpty();
    }

    @Test
    void setSupportedLanguages_is_set() {
        var tenantSetting = new TenantSetting();
        tenantSetting.setSupportedLanguages(List.of(AppLanguages.DE));
        Assertions.assertThat(tenantSetting.getSupportedLanguages())
            .isNotEmpty()
            .hasSize(1)
            .contains(AppLanguages.DE);
        tenantSetting.setSupportedLanguages(List.of(AppLanguages.DE, AppLanguages.FR));
        Assertions.assertThat(tenantSetting.getSupportedLanguages())
            .isNotEmpty()
            .hasSize(2)
            .contains(AppLanguages.DE, AppLanguages.FR);
    }
}

