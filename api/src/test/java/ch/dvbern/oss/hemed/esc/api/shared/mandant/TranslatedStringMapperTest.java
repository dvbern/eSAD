package ch.dvbern.oss.hemed.esc.api.shared.mandant;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TranslatedStringMapperTest {

    private final TenantSettingsService tenantSettingsServiceMock = Mockito.mock(TenantSettingsService.class);
    private final TranslatedStringMapper sut = new TranslatedStringMapper(tenantSettingsServiceMock);

    @Test
    void all_languages_supported_all_properties_set() {
        mockSettings(AppLanguages.DE, "DE, FR");
        TranslatedString translatedString = sut.toEntity(null, new TranslatedStringJson("test_de", "test_fr"));
        Assertions.assertThat(translatedString.getNameDe())
            .isEqualTo("test_de");
        Assertions.assertThat(translatedString.getNameFr())
            .isEqualTo("test_fr");
    }

    @Test
    void one_language_supported_one_property_set() {
        mockSettings(AppLanguages.DE, "DE");
        TranslatedString translatedString = sut.toEntity(null, new TranslatedStringJson("test_de", null));
        Assertions.assertThat(translatedString.getNameDe())
            .isEqualTo("test_de");
        Assertions.assertThat(translatedString.getNameFr())
            .isEqualTo("test_de");
    }

    @Test
    void default_language_property_not_set() {
        mockSettings(AppLanguages.FR, "DE, FR");
        TranslatedStringJson json = new TranslatedStringJson("test_de", null);
        Assertions.assertThatThrownBy(() -> sut.toEntity(null, json))
            .isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.MISSING_TRANSLATED_STRING");
    }

    @Test
    void supported_language_property_not_set() {
        mockSettings(AppLanguages.DE, "DE, FR");
        TranslatedStringJson json = new TranslatedStringJson("test_de", null);
        Assertions.assertThatThrownBy(() -> sut.toEntity(null, json))
            .isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.MISSING_TRANSLATED_STRING");
    }

    private void mockSettings(@NonNull AppLanguages defaultLanguage, @NonNull String supportedLanguages) {
        Mockito.when(tenantSettingsServiceMock.getTenantSetting())
            .thenReturn(new TenantSetting(
                    defaultLanguage,
                    supportedLanguages,
                    16,
                    new TenantSettingFeatures(
                        FeatureEnabledSetting.ENABLED,
                        FeatureEnabledSetting.ENABLED,
                        FeatureEnabledSetting.ENABLED,
                        FeatureEnabledSetting.ENABLED

                    ),
                    EnumSetUtil.toString(List.of(EinladungStatus.TEILNAHME_ABGESAGT,
                        EinladungStatus.TEILNAHME_ABGESAGT))
                )
            );
    }
}
