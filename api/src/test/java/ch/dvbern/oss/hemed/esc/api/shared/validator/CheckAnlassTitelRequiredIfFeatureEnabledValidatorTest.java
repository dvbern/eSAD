package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckAnlassTitelRequiredIfFeatureEnabledValidatorTest {

    @Mock
    private TenantSettingsService tenantSettingsService;

    @InjectMocks
    private CheckAnlassTitelRequiredIfFeatureEnabledValidator validator;

    @ParameterizedTest
    @CsvSource({
        "false, ,     true",
        "false, Mein Anlass, true",
        "true,  Mein Anlass, true",
        "true,  ,     false",
        "true,  '   ', false",
    })
    void isEntityValid(boolean featureEnabled, @Nullable String titel, boolean expectedResult) {
        // ARRANGE
        when(tenantSettingsService.isAnlassTitleRequired()).thenReturn(featureEnabled);
        Anlass anlass = createAnlass(titel);

        // ACT
        boolean result = validator.isEntityValid(anlass);

        // ASSERT
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Anlass createAnlass(@Nullable String titel) {
        Anlass anlass = new Anlass();
        anlass.setTitel(titel);
        return anlass;
    }
}
