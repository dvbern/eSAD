package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.EnumSet;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.shared.types.LoginMethod;
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

    @Test
    void setLoginMethods_is_empty_if_empty() {
        var tenantSetting = new TenantSetting();
        tenantSetting.setLoginMethods(List.of());
        Assertions.assertThat(tenantSetting.getLoginMethods()).isEmpty();
    }

    @Test
    void setLoginMethods_is_set_single() {
        var tenantSetting = new TenantSetting();
        tenantSetting.setLoginMethods(List.of(LoginMethod.DVBERN_HEMED_ENTRA));
        Assertions.assertThat(tenantSetting.getLoginMethods())
            .isEqualTo(EnumSet.of(LoginMethod.DVBERN_HEMED_ENTRA));
    }

    @Test
    void setLoginMethods_is_set_multiple() {
        var tenantSetting = new TenantSetting();
        tenantSetting.setLoginMethods(List.of(LoginMethod.DVBERN_HEMED_ENTRA, LoginMethod.PASSWORD, LoginMethod.BE_LOGIN));
        Assertions.assertThat(tenantSetting.getLoginMethods())
            .isEqualTo(EnumSet.of(LoginMethod.DVBERN_HEMED_ENTRA, LoginMethod.PASSWORD, LoginMethod.BE_LOGIN));
    }
}

