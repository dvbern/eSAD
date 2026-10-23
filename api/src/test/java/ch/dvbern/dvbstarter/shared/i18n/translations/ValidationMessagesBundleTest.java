package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Hibernate Validator preloads the "ValidationMessages" bundle once per locale in "quarkus.locales"
 * and afterwards refuses to interpolate a message for a locale it could not preload (HV000250).
 * A client asking for such a locale turned every constraint violation into an unhandled 500 instead
 * of the intended validation error.
 * <p>
 * Where a bundle comes from matters, too: when the JDK finds only the base bundle for a locale, it
 * prefers the bundle of the JVM's default locale. So every translated language needs its own file,
 * or its users get the texts of whatever language the server happens to default to.
 * <p>
 * {@link AppLanguagesSyncTest} keeps {@link AppLanguages} in sync with "quarkus.locales".
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ValidationMessagesBundleTest {

    private static final String BUNDLE_NAME = "ValidationMessages";

    /** Languages without validation messages of their own yet; they get those of the JVM's default locale. */
    private static final Set<AppLanguages> UNTRANSLATED = EnumSet.of(AppLanguages.IT);

    /** Same locator setup Quarkus uses for the application's ValidationMessages. */
    private static final PlatformResourceBundleLocator LOCATOR = new PlatformResourceBundleLocator(
        BUNDLE_NAME,
        Arrays.stream(AppLanguages.values()).map(AppLanguages::javaLocale).collect(Collectors.toSet()),
        classLoader(),
        true);

    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void hibernate_validator_can_preload_the_bundle_for_every_supported_language(AppLanguages appLang) {
        assertThatCode(() -> LOCATOR.getResourceBundle(appLang.javaLocale()))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void translated_languages_get_their_own_texts_whatever_the_jvm_default_locale(AppLanguages requested) {
        if (UNTRANSLATED.contains(requested)) {
            return;
        }
        Locale locale = requested.javaLocale();

        for (AppLanguages jvmDefault : AppLanguages.values()) {
            ResourceBundle bundle = ResourceBundle.getBundle(
                BUNDLE_NAME,
                locale,
                classLoader(),
                resolvingLikeAJvmDefaultingTo(jvmDefault.javaLocale()));

            assertThat(bundle.getLocale().getLanguage())
                .as("language of the bundle for %s on a JVM defaulting to %s", locale, jvmDefault.javaLocale())
                .isEqualTo(locale.getLanguage());
        }
    }

    /**
     * The German file is the reference: German is the default language, and the base bundle is empty.
     */
    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void translations_have_the_same_keys_as_the_german_one(AppLanguages appLang) throws IOException {
        Set<String> germanKeys = readKeys(AppLanguages.DE)
            .orElseThrow(() -> new AssertionError("ValidationMessages_de.properties is missing"));

        Optional<Set<String>> keys = readKeys(appLang);

        assertThat(keys.isPresent())
            .as("ValidationMessages_%s.properties exists", appLang.javaLocale().getLanguage())
            .isNotEqualTo(UNTRANSLATED.contains(appLang));
        keys.ifPresent(k -> assertThat(k).containsExactlyInAnyOrderElementsOf(germanKeys));
    }

    /**
     * Resolves bundles the way the JDK does on a JVM whose default locale is {@code jvmDefault}, without
     * changing the default locale of the JVM running the tests - and without the JDK's cache, which would
     * hand out the result of one simulated default for the next.
     */
    private static ResourceBundle.Control resolvingLikeAJvmDefaultingTo(Locale jvmDefault) {
        return new ResourceBundle.Control() {
            @Override
            public Locale getFallbackLocale(String baseName, Locale locale) {
                return locale.equals(jvmDefault) ? null : jvmDefault;
            }

            @Override
            public long getTimeToLive(String baseName, Locale locale) {
                return TTL_DONT_CACHE;
            }
        };
    }

    /**
     * Reads one properties file directly instead of going through {@link ResourceBundle}, whose
     * parent chaining would hide the keys a translation is missing.
     */
    private static Optional<Set<String>> readKeys(AppLanguages appLang) throws IOException {
        String fileName = BUNDLE_NAME + "_" + appLang.javaLocale().getLanguage() + ".properties";
        try (InputStream is = classLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                return Optional.empty();
            }
            return Optional.of(new PropertyResourceBundle(is).keySet());
        }
    }

    private static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
