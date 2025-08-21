package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AppLanguagesSyncTest {
    @ConfigProperty(name = "quarkus.locales")
    List<String> quarkusLocales;

    @Test
    void quarkus_locales_is_in_sync_with_AppLanguages_enum_and_has_same_ordering() {
        var locales = Arrays.stream(AppLanguages.values())
            .map(AppLanguages::javaLocale)
            .map(Locale::toLanguageTag)
            .toList();

        assertThat(quarkusLocales)
            .containsExactlyElementsOf(locales);
    }

}
